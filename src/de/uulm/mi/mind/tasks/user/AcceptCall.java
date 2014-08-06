package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.servlet.StreamerServlet;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class AcceptCall extends UserTask<Arrival, Information> {

    @Override
    public boolean validateInput(Arrival object) {
        return true;
    }

    @Override
    public Information doWork(final Active active, Arrival object, boolean compact) {
        User u = (User) active.getAuthenticated();
        String roomIp = null;
        if (u.getEmail().equals("b@b.b")) {
            roomIp = "134.60.128.44";

        } else if (u.getEmail().equals("a@a.a")) {
            roomIp = "134.60.128.47";
        }

        if (roomIp == null) return new Error(Error.Type.SECURITY);
        log.log("Calling", "Streamer is" + StreamerServlet.streamer);
        if (StreamerServlet.streamer != null) {
            StreamerServlet.streamer.onMessage(roomIp, "accept");
        }
        return new Success("call accepted");
    }

    @Override
    public String getTaskName() {
        return "accept_call";
    }

    @Override
    public Class<Arrival> getInputType() {
        return Arrival.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
