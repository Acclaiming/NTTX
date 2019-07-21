package io.kurumi.ntt.fragment.spam;

import io.kurumi.ntt.db.*;

public class ReportLimit {
		
		public static Data<ReportLimit> data = new Data<ReportLimit>(ReportLimit.class);
		
		public Long id;
		
		public Long until;
		
		public String reason;
		
}
