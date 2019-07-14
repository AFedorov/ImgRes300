package tech.fivebit;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static tech.fivebit.CvUtilsFX.MatToBufferedImage;

public class ImgRes300 {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private void run(String inDir, String outDir) {
        char[] animationChars = new char[] {'|', '/', '-', '\\'};
//todo release all images
        File f = new File(inDir);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            Mat croppedImage = getCropImage(inDir + "\\" + files[i].getName());
            Mat resizedImage = new Mat();
            Imgproc.resize(croppedImage, resizedImage, new Size(201, 283), 0, 0,
                    Imgproc.INTER_AREA);

            Imgcodecs.imwrite(outDir + "\\" + files[i].getName(), resizedImage);
            try {
                saveAsJPEG300dpi(MatToBufferedImage(resizedImage), new FileOutputStream(new File(outDir + "\\" + files[i].getName())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print("Processing: "  + files[i].getName() + " " + 100/files.length*i + "% " + animationChars[i % 4] + '\r');
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    private Mat getCropImage(String imageFileName) {
        Mat sourceImage = Imgcodecs.imread(imageFileName);
        if (sourceImage.empty()) {
            System.out.println("Не удалось загрузить изображение");
//            return;
        }

        CascadeClassifier face_detector = new CascadeClassifier();
        String path = "C:\\opencv\\sources\\data\\haarcascades\\";
        String name = "haarcascade_frontalface_alt.xml";
        if (!face_detector.load(path + name)) {
            System.out.println("Не удалось загрузить классификатор " + name);
//            return;
        }

        MatOfRect faces = new MatOfRect();
        face_detector.detectMultiScale(sourceImage, faces);
        Rect r = faces.toArray()[0];

        float ratio = 1.7f;
        int heightCrop = (int) (r.height*ratio);
        if (heightCrop > sourceImage.height()) {
            heightCrop = sourceImage.height();
        }
        int widthCrop = (int) (201*((double)heightCrop/283));
        if (sourceImage.height() < heightCrop) {System.out.println("Image height > crop height");}

        Point centerPoint = new Point(r.x + (int)((double)r.width/2), r.y + (int)((double)r.height/2)*0.87 );

        Rect rectCrop = new Rect((int) (centerPoint.x - (int) ((double)widthCrop/2)),
                (int) (centerPoint.y - (int) ((double)heightCrop/2)),
                widthCrop,
                heightCrop);

        return new Mat(sourceImage,rectCrop);
    }

    public static void saveAsJPEG300dpi(BufferedImage image_to_save, FileOutputStream fos ) throws IOException {

        JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(fos);
        JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);
        jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
        jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
        jpegEncodeParam.setQuality(1f,false);
        jpegEncodeParam.setXDensity(300);
        jpegEncodeParam.setYDensity(300);
        jpegEncoder.encode(image_to_save, jpegEncodeParam);
        image_to_save.flush();
    }


    public static void main(String[] args) {
        String inDir = "in", outDir = "out";

        if (args.length == 1) inDir = args[0];
        if (args.length == 2) {inDir = args[0]; outDir = args[1];};
        long aTime = System.currentTimeMillis();
        new ImgRes300().run(inDir, outDir);
        long bTime = System.currentTimeMillis();
        System.out.println(bTime - aTime + " ms");
//        launch(args);
    }

}
