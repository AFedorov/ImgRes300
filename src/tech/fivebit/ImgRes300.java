package tech.fivebit;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static tech.fivebit.CvUtilsFX.MatToBufferedImage;

public class ImgRes300 {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Mat getCropImage(Mat sourceImage, CascadeClassifier faceCascade) {
        int heightCrop = 0, widthCrop = 0;


        if ( (float) sourceImage.width() / 201 >= (float) sourceImage.height() / 283) {
            widthCrop = (int) (((float) sourceImage.height() / 283) * 201);
            heightCrop = sourceImage.height();
        } else {
            widthCrop = sourceImage.width();
            heightCrop = (int) (((float) sourceImage.width() / 201) * 283);
        }

        Rect rectCrop = new Rect(
                (int) (sourceImage.width() - widthCrop) /2,
                (int) (sourceImage.height() - heightCrop) /2,
                widthCrop, heightCrop);
        Mat croppedImage = sourceImage.submat(rectCrop);
        return croppedImage;
    }


//    private Mat getCropImage(Mat sourceImage, CascadeClassifier faceCascade) {
//        float ratio = (float) 201 / 283; // aspect ratio width / height =~ 0.71
//        float enlargementRatio = 1.8f;
//
//        Mat sourceImageGray = new Mat();
//        Imgproc.cvtColor(sourceImage, sourceImageGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.equalizeHist(sourceImageGray, sourceImageGray);
//
//        // -- Detect faces
//        MatOfRect faces = new MatOfRect();
//        faceCascade.detectMultiScale(sourceImageGray, faces);
//
//        List<Rect> listOfFaces = faces.toList();
//        if ( listOfFaces.size() == 0 ) {
//            System.out.println("Face rect not found");
//            return null;
//        }
//        Rect r = listOfFaces.get(0); //first and only
//
////        if ((int) (r.height * enlargementRatio) <= sourceImage.height())
//        int heightCrop = (int) (r.height * enlargementRatio);
//        int widthCrop = (int) (ratio * heightCrop);
//        Point centerPoint = new Point(r.x + (int) ((double) r.width / 2), r.y + (int) ((double) r.height / 2) * 0.87);
//        if (heightCrop > sourceImage.height() || widthCrop > sourceImage.width()) {
//
//            heightCrop = sourceImage.height();
//            widthCrop = (int) (heightCrop / ratio);
//            centerPoint.x = widthCrop / 2;
//            centerPoint.y = heightCrop / 2;
//
//
//            if ( ((float) sourceImage.height() / sourceImage.width()) < ratio )  System.out.println("height < width");
//            else System.out.println(" height > width ");
//
////            System.out.println("Image height > crop height");
////            heightCrop = sourceImage.height();
////            widthCrop = (int) (ratio * heightCrop);
////            centerPoint.x = widthCrop / 2;
////            centerPoint.y = heightCrop / 2;
//        }
//
//        Rect rectCrop = new Rect((int) (centerPoint.x - (int) ((double) widthCrop / 2)),
//                (int) (centerPoint.y - (int) ((double) heightCrop / 2)) > 0 ? (int) (centerPoint.y - (int) ((double) heightCrop / 2)) :0,
//                widthCrop, heightCrop);
//
//
////                new Rect((int) (centerPoint.x - (int) ((double) widthCrop / 2)),
////                (int) (centerPoint.y - (int) ((double) heightCrop / 2)),
////                widthCrop,
////                heightCrop);
//        Mat croppedImage = sourceImage.submat(rectCrop);
//        return croppedImage;
//    }

    private void saveAsJPEG300dpi(BufferedImage image_to_save, FileOutputStream fos) {

        JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(fos);
        JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);
        jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
        jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
        jpegEncodeParam.setQuality(1f, false);
        jpegEncodeParam.setXDensity(300);
        jpegEncodeParam.setYDensity(300);
        try {
            jpegEncoder.encode(image_to_save, jpegEncodeParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image_to_save.flush();
    }

    private void run(String[] args) {
        char[] animationChars = new char[]{'|', '/', '-', '\\'};
        String inDir = args.length > 0 ? args[0] : "in";
        String outDir = args.length > 1 ? args[1] : "out";

        CascadeClassifier faceCascade = new CascadeClassifier();
        if (!faceCascade.load("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml")) {
            System.err.println("--(!)Error loading face cascade: C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");
            System.exit(0);
        }

        File f = new File(inDir);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.print("Обрабатывается: " + files[i].getName() + '\r');
            Mat sourceImage = Imgcodecs.imread(inDir + "//" + files[i].getName());
            if (sourceImage.width() == 0) {
                System.out.println("Error width = 0 " + files[i].getName());
                continue;
            }

            Mat croppedImage = getCropImage(sourceImage, faceCascade);
            if (croppedImage != null) {
                Mat resizedImage = new Mat();
                Imgproc.resize(croppedImage, resizedImage, new Size(201, 283), 0, 0,
                        Imgproc.INTER_AREA);

//            Imgcodecs.imwrite(outDir + "\\" + files[i].getName(), resizedImage);
                try {
                    saveAsJPEG300dpi(MatToBufferedImage(resizedImage), new FileOutputStream(new File(outDir + "\\" + files[i].getName())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println(" Обработан: " + files[i].getName());
//            System.out.print("Processing: "  + files[i].getName() + " " + 100/files.length*i + "% " + animationChars[i % 4] + '\r');
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            croppedImage.release();
//            resizedImage.release();
            }
        }
    }

    public static void main(String[] args) {
        long aTime = System.currentTimeMillis();
        new ImgRes300().run(args);
        long bTime = System.currentTimeMillis();
        System.out.println(bTime - aTime + " ms");
    }

}
