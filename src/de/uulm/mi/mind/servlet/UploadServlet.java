package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * Servlet implementation class UploadServlet
 * uploads image to the 'image' folder of the deployed server - as 'map.'extension
 */
// original source: http://www.tutorialspoint.com/servlets/servlets-file-uploading.htm
// todo: create folders for image paths if they don't exist!
// todo ignore image ending for naming (simplifies a lot :P)
public class UploadServlet extends HttpServlet {
    private final String TAG = "UploadServlet";
    private final int maxFileSize = 5000 * 1024;
    private final int maxMemSize = 5000 * 1024;

    private Messenger log;
    private String filePath;
    private String SEP = System.getProperty("file.separator");
    private ServletFileUpload upload;

    @Override
    public void init() throws ServletException {
        super.init();
        log = Messenger.getInstance();

        // set base directory for files to be stored in
        filePath = this.getServletContext().getRealPath("/") + "images" + SEP;
        // Create factory with file size and path
        // DiskFileItemFactory factory = new DiskFileItemFactory(maxMemSize, new File(filePath));
        // Create a new file upload handler for map
        upload = new ServletFileUpload(new DiskFileItemFactory());
        // maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        log.log(TAG, "Created.");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {

        // must be multipart
        if (!(ServletFileUpload.isMultipartContent(request))) {
            log.error(TAG, "Request was not a multipart content!");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // read form
        List<FileItem> fileItems;
        try {
            fileItems = upload.parseRequest(request);
        } catch (FileUploadException e) {
            e.printStackTrace();
            log.error(TAG, "Failed to parse request!");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // now get some form of authentication
        String email = null, password = null, session = null;
        FileItem image = null;
        for (FileItem item : fileItems) {
            // catch image
            if (!(item.isFormField())) {
                image = item;
                continue;
            }
            // handle extra data
            switch (item.getFieldName()) {
                case "email":
                    email = item.getString();
                    break;
                case "password":
                    password = item.getString();
                    break;
                case "session":
                    session = item.getString();
                    break;
                default:
                    log.error(TAG, "Unknown upload form data submitted, ignoring!");
                    break;
            }
        }
        // check if session might be in cookie if session hasn't been set yet
        if (session == null && request.getCookies().length > 1) {
            session = request.getCookies()[1].getValue();
        }
        // now check if we have some value we can work with
        Active active;
        if (email != null && password != null) {
            // MUST first check these values, sometimes old sessions are still floating around!!!
            // todo check why cookies are never cleaned!
            active = Security.begin(new User(email, password), null);
        } else if (session != null) {
            active = Security.begin(null, session);
        } else {
            log.error(TAG, "Failed to find authentication!");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // check active
        if (active == null) {
            log.error(TAG, "Failed authentication!");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // make sure we have a user
        if (!(active.getAuthenticated() instanceof User)) {
            log.error(TAG, "Wrong authenticated type tried image upload!");
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        // make sure we have an image
        if (image == null) {
            log.error(TAG, "No image found to upload!");
            response.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        // now get and check action
        String action = request.getPathInfo().substring(1);
        User user = ((User) active.getAuthenticated());
        switch (action) {
            case "map":
                if (!(user.isAdmin())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    log.error(TAG, "Nonadmin " + user.readIdentification() + "tried to upload new map!");
                    return;
                }
                // now write image
                if (writeImage(image, "map", filePath)) {
                    log.log(TAG, "New map uploaded.");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }

                break;
            case "icon":
                if (writeImage(image, "icon_" + user.readIdentification(), filePath + "custom_icons" + SEP)) {
                    log.log(TAG, "User " + user.readIdentification() + " uploaded new icon.");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                break;
            default:
                log.error(TAG, "Unknown action!");
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
        }

        Security.finish(active);
        // todo what do we send back here?
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Given a FileItem and a name, tries to write the image. Will override if an image of the same name already
     * exists!
     *
     * @param item      The FileItem containing the image.
     * @param imageName The name of the image.
     * @return True if it worked, false elsewise.
     */
    private boolean writeImage(FileItem item, String imageName, String path) {
        // Write the file
        File file;
        if (imageName.lastIndexOf("\\") >= 0) {
            file = new File(path +
                    imageName.substring(imageName.lastIndexOf("\\")));
        } else {
            file = new File(path +
                    imageName.substring(imageName.lastIndexOf("\\") + 1));
        }
        try {
            new File(path).mkdir();
            item.write(file);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(TAG, "Failed to write image!");
            return false;
        }
        return true;
    }
}


