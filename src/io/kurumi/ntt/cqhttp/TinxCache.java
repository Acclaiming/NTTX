package io.kurumi.ntt.cqhttp;

import java.util.HashMap;
import io.kurumi.ntt.cqhttp.model.GroupInfo;
import io.kurumi.ntt.cqhttp.model.GroupMember;

public class TinxCache {
	
	private HashMap<Long,GroupInfo> groupInfoCache = new HashMap<>();
	private HashMap<Long,GroupMember> groupMemberCache = new HashMap<>();
	
}
