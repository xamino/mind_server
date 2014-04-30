package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.json.JsonWrapper;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.Departure;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class UploadServlet
 * uploads image to the 'image' folder of the deployed server - as 'map.'extension
 */
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
    private final String TAG = "UploadServlet";
    private final int maxFileSize = 5000 * 1024;
    private final int maxMemSize = 5000 * 1024;
    private Messenger log;
    private JsonWrapper json;
    private String filePath;
    private File file;

    @Override
    public void init() throws ServletException {
        super.init();
        json = JsonWrapper.getInstance();
        log = Messenger.getInstance();
        log.log(TAG, "Created.");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {

        // check if valid
        // 2nd cookie contains our session hash that we can check
        String session = request.getCookies()[1].getValue();
        Active active = Security.begin(null, session);
        Data answer = null;
        if (active == null) {
            log.error(TAG, "Session invalid!");
            answer = new Error(Error.Type.SECURITY, "Invalid Session!");
        } else {
            answer = uploadLogic(request);
        }
        Security.finish(active);
        prepareDeparture(response, answer);
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, java.io.IOException {

        throw new ServletException("GET method used with " +
                getClass().getName() + ": POST method required.");
    }

    private Data uploadLogic(HttpServletRequest request) {
        filePath = request.getSession().getServletContext().getRealPath("/") + "images" + System.getProperty("file.separator");

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new Error(Error.Type.WRONG_OBJECT, "Not a multipart upload!");
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(maxMemSize);
        // Location to save data that is larger than maxMemSize.
        factory.setRepository(new File(filePath));

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        try {
            // Parse the request to get file items.
            List<FileItem> fileItems = upload.parseRequest(request);

            // Process the uploaded file items

            for (FileItem fi : fileItems) {
                if (!fi.isFormField()) {
                    // Get the uploaded file parameters
                    String fileName = fi.getName();
                    String ext = "." + FilenameUtils.getExtension(fileName);
                    fileName = "map" + ext;

                    // Write the file
                    if (fileName.lastIndexOf("\\") >= 0) {
                        file = new File(filePath +
                                fileName.substring(fileName.lastIndexOf("\\")));
                    } else {
                        file = new File(filePath +
                                fileName.substring(fileName.lastIndexOf("\\") + 1));
                    }
                    fi.write(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(TAG, "Upload failed!");
            return new Error(Error.Type.ILLEGAL_VALUE, "Upload failed!");
        }
        return new Success("Upload successful.");
    }

    /**
     * Creates JSON object out of answer and encapsulates it in the type object.
     *
     * @param response The response where the data will be written.
     * @param answer   The object to attach.
     * @throws java.io.IOException
     */
    private void prepareDeparture(HttpServletResponse response, Data answer) throws IOException {
        // Must be done before write:
        response.setCharacterEncoding("UTF-8");
        // If this happens, send back a standard error message.
        if (answer == null) {
            log.error(TAG, "Empty ANSWER! Should never happen!");
            answer = new Error(Error.Type.WRONG_OBJECT, "Answer does not contain an object! Make sure your request is valid!");
        }
        Departure dep = new Departure(answer);
        String jsonBack = json.toJson(dep);
        response.getWriter().write(jsonBack);
        response.setContentType("application/json");
    }
}


