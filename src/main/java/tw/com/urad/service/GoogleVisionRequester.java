package tw.com.urad.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Created by kuowenhao on 2016/12/22.
 */
public class GoogleVisionRequester {

    private static final String APPLICATION_NAME = "facebook-report-domo";
    private static Vision vision;
    private RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws Exception {

        vision = getVisionService();
        GoogleVisionRequester visionRequester = new GoogleVisionRequester();
        visionRequester.picProperties();
    }

    public static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

       return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<EntityAnnotation> picProperties() throws Exception {

        Resource imageResource
            = restTemplate.getForObject("https://scontent.xx.fbcdn.net/t45.1600-4/12426273_6044123473745_505776075_n.png", Resource.class);

        Image image = new Image();
        image.encodeContent(IOUtils.toByteArray(imageResource.getInputStream()));

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
                                        .setMaxResults(4),
                                new Feature()
                                        .setType("LOGO_DETECTION")
                                        .setMaxResults(4)));

        Vision.Images.Annotate annotate =
                vision.images()
                    .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();

        AnnotateImageResponse response = batchResponse.getResponses().get(0);

        response.entrySet().stream().map(entry -> entry.getKey() + "  " + entry.getValue()).forEach(System.out::println);

        return response.getLandmarkAnnotations();
    }
}
