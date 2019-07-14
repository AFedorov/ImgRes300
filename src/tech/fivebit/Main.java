package trch.fivebit;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.w3c.dom.Element;
import tech.fivebit.CvUtilsFX;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import static tech.fivebit.CvUtilsFX.MatToBufferedImage;

//import java.awt.*;

public class Main extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    @Override
    public void start(Stage primaryStage) throws Exception{
        VBox root = new VBox(15.);
        root.setAlignment(Pos.CENTER);

        Button button = new Button("Run");
        button.setOnAction(this::onClickButton);
        root.getChildren().add(button);

        Scene scene = new Scene(root, 400.0, 150.0);
        primaryStage.setTitle("OpenCV " + Core.VERSION);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
        });
        primaryStage.show();
    }

    private void onClickButton(ActionEvent e) {
        Mat cropImg = new Mat();
        Mat img = Imgcodecs.imread("C:\\JP\\data\\a.jpg");
        if (img.empty()) {
            System.out.println("Не удалось загрузить изображение");
            return;
        }

        CascadeClassifier face_detector = new CascadeClassifier();
        String path = "C:\\opencv\\sources\\data\\haarcascades\\";
        String name = "haarcascade_frontalface_alt.xml";
        if (!face_detector.load(path + name)) {
            System.out.println("Не удалось загрузить классификатор " + name);
            return;
        }
        MatOfRect faces = new MatOfRect();
        face_detector.detectMultiScale(img, faces);

        for (Rect r : faces.toList()) {

            System.out.println(String.format("facesArray[0].x %s", r.x));
            System.out.println(String.format("facesArray[0].y %s", r.y));
            System.out.println(String.format("facesArray[0].width %s", r.width));
            System.out.println(String.format("facesArray[0].height %s", r.height));

//            float diff = r.width
            float ratio = 1.7f;


            int heightCrop = (int) (r.height*ratio);
            if (heightCrop > img.height()) {
                heightCrop = img.height();
            }
            int widthCrop = (int) (201*((double)heightCrop/283));



//            if (img.width() > widthCrop) {
//                widthCrop = img.width();
//                heightCrop = (int) (img.height()*ratio);
//                System.out.println("Image width > crop width");
//            }
            if (img.height() > heightCrop) {System.out.println("Image height > crop height");}

            Point centerPoint = new Point(r.x + (int)((double)r.width/2), r.y + (int)((double)r.height/2)*0.81 );

            Rect rectCrop = new Rect((int) (centerPoint.x - (int) ((double)widthCrop/2)),
                    (int) (centerPoint.y - (int) ((double)heightCrop/2)),
                    widthCrop,
                    heightCrop);

            cropImg = new Mat(img,rectCrop);


//            Imgproc.rectangle(img, new Point(r.x - r.width/4, r.y-r.height/4),
//                    new Point(r.x + r.width, r.y + r.height * 2),
//                    new Scalar(153, 217, 234), 2);
//                    CvUtils.COLOR_WHITE, 2);
        }

//        double fx = 201 / cropImg.width();
//        double fy = 201 / cropImg.width();
        Mat img2 = new Mat();
        Imgproc.resize(cropImg, img2, new Size(201, 283), 0, 0,
                Imgproc.INTER_AREA);

        CvUtilsFX.showImage(img2, "Текст в заголовке окна");
//        Imgcodecs.imwrite("c:\\jp\\out.jpg", img2);


        try {
            saveAsJPEG(MatToBufferedImage(img2), new FileOutputStream(new File("c:\\jp\\out1.jpg")));
        } catch (IOException e1) {
            e1.printStackTrace();
        }


//        try {
//            saveAsJPEG("300", MatToBufferedImage(img2), 1f, new FileOutputStream(new File("c:\\jp\\out1.jpg")));
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }

        img.release();
//        try {
//            saveGridImage(new File("c:\\jp\\out.jpg"));
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
    }

    public static void saveAsJPEG( BufferedImage image_to_save, FileOutputStream fos ) throws IOException {

//        BufferedImage image =ImageIO.read(newFile(path));
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

    public static void saveAsJPEG(String jpgFlag, BufferedImage image_to_save, float JPEGcompression, FileOutputStream fos) throws IOException {

        //useful documentation at http://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html
        //useful example program at http://johnbokma.com/java/obtaining-image-metadata.html to output JPEG data

        //old jpeg class
        //com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(fos);
        //com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);

        // Image writer
        JPEGImageWriter imageWriter = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
        imageWriter.setOutput(ios);

        //and metadata
        IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image_to_save), null);

        if (jpgFlag != null){

            int dpi = 300;

            try {
                dpi = Integer.parseInt(jpgFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //old metadata
            //jpegEncodeParam.setDensityUnit(com.sun.image.codec.jpeg.JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            //jpegEncodeParam.setXDensity(dpi);
            //jpegEncodeParam.setYDensity(dpi);

            //new metadata
            Element tree = (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", "300");
            jfif.setAttribute("Ydensity", "300");
            jfif.setAttribute("resUnits", "1");
            imageMetaData.mergeTree("javax_imageio_jpeg_image_1.0", tree);

        }

        if(JPEGcompression >= 0 && JPEGcompression <= 1f){

            //old compression
            //jpegEncodeParam.setQuality(JPEGcompression,false);

            // new Compression
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(JPEGcompression);

        }

        //old write and clean
        //jpegEncoder.encode(image_to_save, jpegEncodeParam);

        //new Write and clean up
        imageWriter.write(imageMetaData, new IIOImage(image_to_save, null, null), null);
        ios.close();
        imageWriter.dispose();

    }


//    private Mat cropImage () {
//
//    }

//    public static final String DENSITY_UNITS_NO_UNITS ="00";
//    public static final String DENSITY_UNITS_PIXELS_PER_INCH ="01";
//    public static final String DENSITY_UNITS_PIXELS_PER_CM ="02";
//    private BufferedImage gridImage;

//    private void saveGridImage(File output) throws IOException {
//        output.delete();
//
//        final String formatName = "jpeg";
//
//        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext(); ) {
//            ImageWriter writer = iw.next();
//            ImageWriteParam writeParam = writer.getDefaultWriteParam();
//            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
//            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
//            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
//                continue;
//            }
//
//            setDPI(metadata);
//            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
//            try {
//                writer.setOutput(stream);
//                writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
//            } finally {
//                stream.close();
//            }
//            break;
//        }
//    }
//
//    private static void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {
//        String metadataFormat = "javax_imageio_jpeg_image_1.0";
//        IIOMetadataNode root = new IIOMetadataNode(metadataFormat);
//        IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
//        IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");
//        IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
//        app0JFIF.setAttribute("majorVersion", "1");
//        app0JFIF.setAttribute("minorVersion", "2");
//        app0JFIF.setAttribute("thumbWidth", "0");
//        app0JFIF.setAttribute("thumbHeight", "0");
//        app0JFIF.setAttribute("resUnits", DENSITY_UNITS_PIXELS_PER_INCH);
//        app0JFIF.setAttribute("Xdensity", String.valueOf(300));
//        app0JFIF.setAttribute("Ydensity", String.valueOf(300));
//
//        root.appendChild(jpegVariety);
//        root.appendChild(markerSequence);
//        jpegVariety.appendChild(app0JFIF);
//
//        metadata.mergeTree(metadataFormat, root);
//    }


//    private void run() {
//        char[] animationChars = new char[] {'|', '/', '-', '\\'};
//
//        File f = new File("\\in");
//        File[] files = f.listFiles();
//        for (int i = 0; i < files.length; i++) {
//            System.out.println(files[i].getName());
//        }
//
//        for (int i = 0; i < 100; i++) {
//            System.out.print("Processing: " + i + "% " + animationChars[i % 4] + '\r');
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

    public static void main(String[] args) {
//        long aTime = System.currentTimeMillis();
//        new Main().run();
//        long bTime = System.currentTimeMillis();
//        System.out.println(bTime - aTime + " ms");
        launch(args);
    }
}
