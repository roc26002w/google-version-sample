package tw.com.urad;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

/**
 * Created by Rocko on 2016/12/22.
 */
public class DetectLandmark {


    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "facebook-report-domo";

    private static final int MAX_RESULTS = 4;

    public static void main(String[] args) throws IOException, GeneralSecurityException {
//        if (args.length != 1) {
//            System.err.println("Usage:");
//            System.err.printf("\tjava %s gs://<bucket_name>/<object_name>\n",
//                    DetectLandmark.class.getCanonicalName());
//            System.exit(1);
//        } else if (!args[0].toLowerCase().startsWith("gs://")) {
//            System.err.println("Google Cloud Storage url must start with 'gs://'.");
//            System.exit(1);
//        }

        String uri = "https://scontent.xx.fbcdn.net/t45.1600-4/15351875_6059016705329_7795618740367785984_n.png";

        DetectLandmark app = new DetectLandmark(getVisionService());
        String landmarks = app.identifyLandmark(uri, MAX_RESULTS);
        //System.out.printf("Found %d landmark%s\n", landmarks.size(), landmarks.size() == 1 ? "" : "s");
//        for (EntityAnnotation annotation : landmarks) {
//            System.out.printf("\t%s\n", annotation.getDescription());
//        }

        System.out.println(landmarks);

    }

    // [START authenticate]

    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    public static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    // [END authenticate]

    // [START detect_gcs_object]
    private final Vision vision;

    /**
     * Constructs a {@link DetectLandmark} which connects to the Vision API.
     */
    public DetectLandmark(Vision vision) {
        this.vision = vision;
    }

    /**
     * Gets up to {@code maxResults} landmarks for an image stored at {@code uri}.
     */
    public String identifyLandmark(String uri, int maxResults) throws IOException {

        Image image = new Image();
        URL url = new URL(uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(ImageIO.read(url), "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        image.encodeContent(imageInByte);
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(image)
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("SAFE_SEARCH_DETECTION")
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("IMAGE_PROPERTIES")
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("LABEL_DETECTION")
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("TEXT_DETECTION")
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("FACE_DETECTION")
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("LANDMARK_DETECTION")
                                        .setMaxResults(maxResults)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        AnnotateImageResponse response = batchResponse.getResponses().get(0);
        System.out.println("response :: {}" + response);

        return response.toPrettyString();
    }
    // [END detect_gcs_object]

}
