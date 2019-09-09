package io.kurumi.ntt.fragment.spam;

import io.kurumi.ntt.db.Data;

public class VolunteerUser {

    public Data<VolunteerUser> data = new Data<VolunteerUser>(VolunteerUser.class);

    public Long id;

    public boolean confirm;

    public long joinAt;

    public String reason;

}
