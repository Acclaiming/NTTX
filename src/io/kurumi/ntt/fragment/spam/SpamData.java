package io.kurumi.ntt.fragment.spam;

import io.kurumi.ntt.db.Data;

public class SpamData {

    public static Data<SpamData> data = new Data<SpamData>(SpamData.class);

    public Long id;

    public boolean confirm;

    public String reason;

    public Long createAt;

    public Long fromUser;

    public Long fromVolunteer;

}
