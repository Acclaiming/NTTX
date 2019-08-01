package io.kurumi.ntt.fragment.inline;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.utils.Encoder;

public class CoreValueEncode extends Fragment {

	@Override
	public boolean query() {
		
		return true;
		
	}

	Encoder encoder = new Encoder(Encoder.coreValus);
	
	@Override
	public void onQuery(UserData user, Query inlineQuery) {
		
		if (inlineQuery.startsWith("C")) return;
		
		inlineQuery.article("编码完成",encoder.encode(inlineQuery.queryString()),null,null);
		
	}
	
}
