package io.kurumi.ntt.cqhttp.response;

import java.util.List;

public class GetGroupListResponse extends BaseResponse {

    public List<Group> data;

    public static class Group {

        public int group_id;
        public String group_name;

    }

}
