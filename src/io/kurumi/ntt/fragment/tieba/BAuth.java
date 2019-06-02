package io.kurumi.ntt.fragment.tieba;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.tieba.*;

public class BAuth {
	
	public static Data<BAuth> data = new Data<BAuth>(BAuth.class);
	
	public Long id;
	
	public String bduss;
	public String ptoken;
	public String stoken;
	
}
