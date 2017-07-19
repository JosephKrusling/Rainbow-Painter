import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by josep on 7/7/2017.
 */
public class ImageManip {

    public static ArrayList<Point> openList;
    public static byte[] imageData;
    public static boolean[] processed;

    private static final int width = 512;
    private static final int height = 512;
    private static final int colorDepth = 64;

    private static final Random r = new Random();

    public static void main(String[] args) {
        assert(width*height == colorDepth*colorDepth*colorDepth);

        PerformanceMonitor monitor = new PerformanceMonitor();
        monitor.start();

        Color[] colors = new Color[colorDepth*colorDepth*colorDepth];
        for (byte r = 0; r < colorDepth; r++) {
            for (byte g = 0; g < colorDepth; g++) {
                for (byte b = 0; b < colorDepth; b++) {
                    colors[r*colorDepth*colorDepth + g*colorDepth + b] = new Color(r, g, b);
                }
            }
        }
        System.out.println(String.format("Built color array in %d ms", monitor.checkPoint()));

        shuffleArray(colors);
        System.out.println(String.format("Shuffled color array in %d ms", monitor.checkPoint()));

        imageData = new byte[width*height*3];
        processed = new boolean[width*height];
        System.out.println(String.format("Allocated arrays in %d ms", monitor.checkPoint()));

//        openList = new ArrayList<Point>();
        openList = new ArrayList<Point>();
//        openList.add(new Point(width/2, height/2));
//        processed[index(width/2, height/2)] = true;

        for (int i=0; i < 5000; i++) {
            int placeAtX = r.nextInt(width);
            int placeAtY = r.nextInt(height);
            openList.add(new Point(placeAtX, placeAtY));
            processed[index(placeAtX, placeAtY)] = true;
        }

        openList.add(new Point(width/2, height/2));
        processed[index(width/2, height/2)] = true;

        long lastUpdate = System.currentTimeMillis();
        int totalIter = 0;
        int pixelsWrittenSince = 0;

        int peakSize = 0;
        int colorIndex = 0;
        int imageNameNonce = 0;
        String folderName = String.format("Result_%d", System.currentTimeMillis());
        while (!openList.isEmpty()) {
            if (openList.size() > peakSize)
                peakSize = openList.size();

            Color color = colors[colorIndex];
            colorIndex++;

            float bestScore = 999999999;
            Point bestPoint = null;
            for (int i = 0; i < openList.size(); i++) {
                Point testPoint = openList.get(i);
                float thisScore = scorePoint(testPoint, color);
                if (thisScore < bestScore || bestPoint == null) {
                    bestScore = thisScore;
                    bestPoint = testPoint;
                }
            }
//            for (Point testPoint : openList) {
//                float thisScore = scorePoint(testPoint, color);
//                if (thisScore < bestScore || bestPoint == null) {
//                    bestScore = thisScore;
//                    bestPoint = testPoint;
//                }
//            }
            putColor(bestPoint.x, bestPoint.y, color.r, color.g, color.b);
            openList.remove(bestPoint);
            addNeighbors(bestPoint);

            pixelsWrittenSince++;
            totalIter++;

            if (System.currentTimeMillis() - lastUpdate > 10000) {
                lastUpdate = System.currentTimeMillis();
                float percentDone = totalIter / (float)(height*width) * 100;
                writeImageToFile(folderName + File.separator + String.format("step%05d", imageNameNonce));
                imageNameNonce++;
                System.out.println(String.format("%05.2f%% - %d Written Since - %d Per Second - %d Points In List", percentDone, pixelsWrittenSince, pixelsWrittenSince / 10, openList.size()));
                pixelsWrittenSince = 0;
            }
        }
        System.out.println(String.format("Calculated image data in %d ms", monitor.checkPoint()));

        writeImageToFile(folderName + File.separator + "final");
        System.out.println(String.format("Wrote to disk in %d ms", monitor.checkPoint()));



        System.out.println(String.format("Program finished in %d ms", monitor.stop()));
        System.exit(0);
    }

    private static void putColor(int x, int y, byte r, byte g, byte b) {
        int section = (x*width + y)*3;
        imageData[section] = r;
        imageData[section+1] = g;
        imageData[section+2] = b;
    }

    public static Color getColor(int x, int y) {
        int section = (x*width + y)*3;
        byte r = imageData[section];
        byte g = imageData[section+1];
        byte b = imageData[section+2];
        return new Color(r, g, b);
    }

    private static boolean writeImageToFile(String identifier) {
        PerformanceMonitor writeMonitor = new PerformanceMonitor();
        writeMonitor.start();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {
                int section = (x*width + y)*3;
                int r = imageData[section] * 4;
                int g = imageData[section+1] * 4;
                int b = imageData[section+2] * 4;

                int pixel = (r << 16) | (g << 8) | (b);
                outputImage.setRGB(x, y, pixel);
            }
        }

        File saveLocation = new File("./results/" + identifier + ".png");
        saveLocation.mkdirs();
        try {
            ImageIO.write(outputImage, "png", saveLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Wrote image in %d milliseconds", writeMonitor.stop()));
        return false;
    }

    private static float scorePoint(Point point, Color color) {
        float totalDifference = 0;
        int radius = 1;
        int pixelsChecked = 1;
        for (int x = Math.max(point.x - radius, 0); x <= Math.min(point.x + radius, width-1); x++) {
            for (int y = Math.max(point.y - radius, 0); y <= Math.min(point.y + radius, height-1); y++) {
                Color pointColor = getColor(x, y);
                if (pointColor.r != 0 || pointColor.g != 0 || pointColor.b != 0) {
//                    System.out.println("hi");
                    totalDifference += color.difference(pointColor);
                    pixelsChecked++;
                }
            }
        }

//        System.out.println(String.format("Difference: %.2f", totalDifference));
        return pixelsChecked == 0 ? 0 : totalDifference / pixelsChecked;
//        return color.difference(pointColor);
    }

    private static int index(int x, int y) {
        return x * width + y;
    }

    private static void addNeighbors(Point point) {
        int x = point.x;
        int y = point.y;

        for (int testX = Math.max(x-1, 0); testX <= Math.min(x+1, width-1); testX++) {
            for (int testY = Math.max(y-1, 0); testY <= Math.min(y+1, height-1); testY++) {
//                System.out.println(testX + ", " + testY);
                if (!processed[index(testX, testY)]) {
                    openList.add(new Point(testX, testY));
                    processed[index(testX, testY)] = true;
                }
            }
        }

    }

    private static void shuffleArray(Color[] array)
    {
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            if (random.nextFloat() > 0.5)
                continue;
            int swapIndex = random.nextInt(array.length);
            Color thisColor = array[i];
            array[i] = array[swapIndex];
            array[swapIndex] = thisColor;
        }
    }

}
