/*
- This program compresses grayscale images using Huffman coding algorithm.
- Images are considered as matrices.
- Each pixel in image is an entry of matrix and is a number in range [0,255]
  which 0 corresponds to black and 255 corresponds to white.
- Input/Output is by files in input-output folder in project folder.
- Developer: Hamidreza Navaei (@HRNavaei)
- All rights reserved.
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    static int index=0;
    static String comprImgCode;
    static long compressionElapsedTime;
    static int comprImgSize=0,befComprImgSize=0;
    static List<NodePath> nodePaths = new ArrayList<>();
    static Map<Integer,String> comprPxlCodeMap = new HashMap<>();

//  main function calls compress or decompress function according to option that user chooses.
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner stdScn = new Scanner(System.in);
        System.out.printf("Compress and Decompress Images using Huffman Algorithm%n%nEnter the number of desired option:%n1. Compress a new image.%n" +
                "2. Decompress a previously compressed image.%n%n> ");

        while(true){
            String option=stdScn.next();
            if(option.equals("1")){
                compress();
                System.out.printf("Image successfully compressed in %d ns (%d ms).%n",compressionElapsedTime,TimeUnit.MILLISECONDS.convert(compressionElapsedTime, TimeUnit.NANOSECONDS));
                System.out.printf("Image size:%n\t- Before compression: %d Bits%n\t- After compression: %d Bits%n\t- Compression value: %d Bits",befComprImgSize,comprImgSize,befComprImgSize-comprImgSize);
                break;
            }
            else if(option.equals("2")){
                decompress();
                System.out.println("Decompression successfully done.");
                break;
            }
            else
                System.out.printf("Wrong Input%n> ");
        }
    }

/*  This function create a huffman tree by the entries of image matrix while each entry corresponds to a pixel of image,
*/
    //This function does the compression option.
    public static void compress() throws IOException {

        int[][] imgMatrix = new int[2000][2000]; //Image is considered as a matrix.

        List<Integer> pxlList = new ArrayList<>();//This list keep the brightness number of each pixels in image.
        PixelCount[] pxlCntArray = new PixelCount[256];//This array keeps the occurrence number for each pixel.

        Scanner imgScn = new Scanner(new File("input-output\\input image (original).txt"));//Getting the input from file.

        //This loop fills the datastructures mentioned befor by reading the input.
        int n,m;
        for(int i=0;true;){
            Scanner rowScn = new Scanner(imgScn.nextLine());
            for(int j=0;true;) {
                int pxlNum=rowScn.nextInt();

                imgMatrix[i][j]=pxlNum;

                if(pxlCntArray[pxlNum]==null){
                    pxlCntArray[pxlNum]= new PixelCount(pxlNum);
                    pxlList.add(pxlNum);
                }
                pxlCntArray[pxlNum].addFreq();

                j++;
                if(!rowScn.hasNext()){
                    m=j;
                    break;
                }
            }
            i++;
            if(!imgScn.hasNextLine()){
                n=i;
                break;
            }
        }

        befComprImgSize=n*m*8; //size of original image.

//      This priority queue helps to keep matrix elements always in increasing order and fo the Huffman coding algorithm on them.
        MyPriorityQueue priorityQueue = new MyPriorityQueue();

//      Filling the queue by pixels, Considering each pixel as a binary tree with one node.
        for (int i = 0; i < pxlList.size(); i++) {
            BinaryTree element =new BinaryTree(pxlCntArray[pxlList.get(i)].getFreq());
            element.setValue(pxlCntArray[pxlList.get(i)].getBrightness());
            element.setLeaf();
            priorityQueue.offer(element);
        }

        long currTime=System.nanoTime(); //Measuring the current system time.

        /* This loop do the Huffman coding algorithm on the priority queue, so that finally there
        is only one element in queue which is the Huffman Tree of our data.
        */
        while(priorityQueue.getSize()>1){
            BinaryTree leftChild = priorityQueue.poll();
            BinaryTree rightChild = priorityQueue.poll();
            BinaryTree parent = new BinaryTree(leftChild.getRoot()+rightChild.getRoot(),leftChild,rightChild);
            leftChild.setParent(parent);
            rightChild.setParent(parent);
            priorityQueue.offer(parent);
        }
        BinaryTree huffmanTree = priorityQueue.peek(); //This is the Huffman tree of input data.

        genComprImgCodeMap(huffmanTree,"",""); //This function Iterate tree and generates huffman code for all pixels.

        StringBuilder comprImgCode = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                String comprPxlCode=comprPxlCodeMap.get(imgMatrix[i][j]);
                comprImgCode.append(comprPxlCode);
                comprImgSize+=comprPxlCode.length();
            }
        }

        compressionElapsedTime=System.nanoTime()-currTime; //This statement measures the total time elapsed for creating and generating huffman code.

        //Saving the created Huffman tree into a file for decompressing compressed image after.
        new File("input-output\\Compressed Image\\heap.txt").delete();
        ObjectOutputStream fHeap = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("input-output\\Compressed Image\\heap.txt")));
        fHeap.writeObject(huffmanTree);
        new File("input-output\\Compressed Image\\heap.txt").setReadOnly();
        fHeap.close();


        //Printing the Huffman tree in to a text file.
        new File("input-output\\Compressed Image\\Heap Print.txt").delete();
        OutputStreamWriter fHeapPrint = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("input-output\\Compressed Image\\Heap Print.txt")));
        for(NodePath e:nodePaths){
            fHeapPrint.write(String.format("%s (%d)%n",e.getPath(),e.getValue()));
        }
        fHeapPrint.close();


        //Saving the generated Huffman code for input image alongside its dimensions.
        new File("input-output\\Compressed Image\\compressed image.txt").delete();
        OutputStreamWriter fComprImg = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("input-output\\Compressed Image\\compressed image.txt")));
        fComprImg.write(String.format("%d %d%n",n,m));
        fComprImg.write(comprImgCode.toString());
        fComprImg.close();
        new File("input-output\\Compressed Image\\compressed image.txt").setReadOnly();
    }

    //This function does the decompression option.
    public static void decompress() throws IOException, ClassNotFoundException {
        // Loading the Huffman tree from file.
        ObjectInputStream fHeap = new ObjectInputStream(new BufferedInputStream(new FileInputStream("input-output\\Compressed Image\\heap.txt")));
        BinaryTree huffmanTree = (BinaryTree)fHeap.readObject();
        fHeap.close();

        //Scanner for scanning the compressed image.
        Scanner comprImgScn = new Scanner(new File("input-output\\Compressed Image\\compressed image.txt"));
        int n=comprImgScn.nextInt(),m=comprImgScn.nextInt(); //Keeping the dimensions.
        comprImgCode=comprImgScn.next(); //This keeps the Huffman code of compressed image.

        //OutputStream variable for saving the decompressed image.
        OutputStreamWriter fDecomprImg = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("input-output\\output image (decompressed).txt")));

        //This matrix is for storing the decompressed image into it for probable use in future(not used in this project).
        int[][] matrix = new int[n][m];

        /*This loop writes the equivalent pixel-brightnesses of the Huffman code into output file in matrix format
        and also storing in the matrix mentioned above.
        */
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int pxlBrightness=decompressCode(huffmanTree); //Calling the function that iterate the tree to find the equivalent brightness.
                matrix[i][j]=pxlBrightness;
                fDecomprImg.write(pxlBrightness + " ");
            }
            fDecomprImg.write(String.format("%n"));//Writing new-line character in output to keep the matrix format.
        }
        fDecomprImg.close();
    }

    /*This function iterates the Huffman tree to generate and store the Huffman code for each pixel
    in respective data structures defined in main class. Also storing the path of each pixel-brightness in
    respective data structure.
     */
    public static void genComprImgCodeMap(BinaryTree binaryTree,String path,String comprPxlCode) throws IOException {
        if(binaryTree.isLeaf()){
            comprPxlCodeMap.put(binaryTree.getValue(),comprPxlCode);
            System.out.printf("%s (%d): %s%n",path+binaryTree.getRoot(),binaryTree.getValue(),comprPxlCode);
            nodePaths.add(new NodePath(binaryTree.getValue(),path+binaryTree.getRoot()));
        }
        else{
            genComprImgCodeMap(binaryTree.getRightChild(),path+binaryTree.getRoot()+"-",comprPxlCode+"1");
            genComprImgCodeMap(binaryTree.getLeftChild(),path+binaryTree.getRoot()+"-",comprPxlCode+"0");
        }
    }

    /*This function iterate the Huffman tree and read the Huffman code of compressed image
    to return the equivalent pixel-brightnesses.
     */
    public static int decompressCode(BinaryTree binaryTree) throws IOException {
        if(binaryTree.isLeaf()){
            return binaryTree.getValue();
        }
        else{
            if(comprImgCode.charAt(index)=='1'){
                index++;
                return decompressCode(binaryTree.getRightChild());
            }
            else{
                index++;
                return decompressCode(binaryTree.getLeftChild());
            }
        }
    }
}

//A Couple for storing a brightness and its frequency
class PixelCount implements Comparable{
    int brightness,freq;

    public PixelCount(int brightness){
        this.brightness=brightness;
        freq=0;
    }

    public void addFreq(){
        freq++;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getFreq() {
        return freq;
    }

    @Override
    public int compareTo(Object o) {
        PixelCount otherPixelCount = (PixelCount) o;
        return this.freq - otherPixelCount.getFreq();
    }
}

//Priority Queue
class MyPriorityQueue{
    List<BinaryTree> binaryTreeQueue;
    int size;

    public MyPriorityQueue(){
        binaryTreeQueue=new ArrayList<>();
        size=0;
    }

    public void offer(BinaryTree binaryTree){
        binaryTreeQueue.add(binaryTree);
        Collections.sort(binaryTreeQueue);
        size++;
    }

    public BinaryTree poll(){
        size--;
        return binaryTreeQueue.remove(0);
    }

    public BinaryTree peek(){
        return binaryTreeQueue.get(0);
    }

    public int getSize() {
        return size;
    }
}

//Data Structure for storing and combining pixels counts in priority queue.
class BinaryTree implements Comparable, Serializable {
    int root,value;
    boolean isLeaf;
    BinaryTree parent,leftChild,rightChild;

    public BinaryTree(int root){
        this.root = root;
        value=-1;
        parent=null;
    }

    public BinaryTree(int root,BinaryTree leftChild,BinaryTree rightChild){
        this.root = root;
        this.leftChild=leftChild;
        this.rightChild=rightChild;
        parent=null;
    }

    public void setParent(BinaryTree parent) {
        this.parent = parent;
    }

    public void setValue(int value){
        this.value=value;
        isLeaf=true;
    }

    public BinaryTree getLeftChild() {
        return leftChild;
    }

    public BinaryTree getRightChild() {
        return rightChild;
    }

    public void setLeaf() {
        this.isLeaf=true;
    }

    public boolean isLeaf(){
        return isLeaf;
    }

    public int getRoot() {
        return root;
    }

    public int getValue() {
        return value;
    }

    //This function cause the sorting to be increasing by the root value.
    @Override
    public int compareTo(Object o) {
        BinaryTree otherBinaryTree = (BinaryTree) o;
        if(this.root == otherBinaryTree.getRoot())
            return 1;
        return (this.root - otherBinaryTree.getRoot());
    }

    public String toString(){
        int parentRoot = parent==null ? -1:parent.getRoot();
        return String.format("(%d,%d from %d)",root,value,parentRoot);
    }
}

//Couple for storing path of each node in Huffman tree.
class NodePath{
    int value;
    String path;

    public NodePath(int brightness,String path){
        this.value=brightness;
        this.path=path;
    }

    public int getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    public String toString(){
        return String.format("(%d: %s)",value,path);
    }
}
