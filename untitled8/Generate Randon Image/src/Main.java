import java.io.*;
import java.util.*;

public class Main {
    public static void main(String... args) throws IOException {
        int[] nOccurrences = new int[6];
//        int[] brightnesses = {0, 0, 0, 150, 150, 150, 150, 150, 150, 150, 50, 50, 50, 50, 100, 100, 100, 100, 100, 100, 200, 200, 250, 250, 250, 250, 250, 250, 250, 250};
        int[] brightnesses = {0, 50, 100, 150, 150, 150, 150, 150, 150, 150, 50, 50, 50, 50, 100, 100, 100, 100, 100, 100, 200, 200, 250, 250, 250, 250, 250, 250, 250, 250};
        OutputStreamWriter fImage = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("input image (original).txt")));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int randomIndex = new Random().nextInt(30);
                fImage.write(String.format("%d ", brightnesses[randomIndex]));
                switch (brightnesses[randomIndex]) {
                    case 0:
                        nOccurrences[0]++;
                        break;
                    case 50:
                        nOccurrences[1]++;
                        break;
                    case 100:
                        nOccurrences[2]++;
                        break;
                    case 150:
                        nOccurrences[3]++;
                        break;
                    case 200:
                        nOccurrences[4]++;
                        break;
                    case 250:
                        nOccurrences[5]++;
                        break;
                }
            }
            fImage.write(String.format("%n"));
        }
        fImage.close();

        System.out.println("---------------------------------");
        System.out.printf("(%d,%d)%n",0,nOccurrences[0]);
        System.out.printf("(%d,%d)%n",50,nOccurrences[1]);
        System.out.printf("(%d,%d)%n",100,nOccurrences[2]);
        System.out.printf("(%d,%d)%n",150,nOccurrences[3]);
        System.out.printf("(%d,%d)%n",200,nOccurrences[4]);
        System.out.printf("(%d,%d)%n",250,nOccurrences[5]);
    }
}