import java.util.regex.Matcher;

public class RaceInfo {
	String full_name;
	String name;
	long total_iterations;
	long total_edges_added;
	boolean static_wdc_only;
	long total_dynamic_instances;
	long min_distance;
	long max_distance;
	int static_trial;
	
	public RaceInfo (String name, boolean static_wdc_only, boolean split_name) {
		this.full_name = name;
		this.name = name;
		if (name.contains("class ")) {
			String [] name_parts = name.split(":");
//			this.name = name_parts[0].split(" ")[1] + ":" + name_parts[1] + /*":" + name_parts[2] +*/ ":" + name_parts[3] + (split_name ? " ->&&&\\\\\n& " : " -> ") + name_parts[4].split("> ")[1].split(" ")[1] + ":" + name_parts[5] + /*":" + name_parts[6] +*/ ":" + name_parts[7];
			this.name = name_parts[0].split(" ")[1] + ":" + name_parts[1] + ":" + name_parts[2] + ":" + name_parts[3] + (split_name ? " ->&&&\\\\\n& " : " -> ") + name_parts[4].split("> ")[1].split(" ")[1] + ":" + name_parts[5] + ":" + name_parts[6] + ":" + name_parts[7];
		} else {
			if (split_name) {
				String [] name_parts = name.split("-> ");
				this.name = name_parts[0] + "->&&&\\\\\n&" + name_parts[1];
			}
		}
		if (this.name.contains("$")) {
			this.name = this.name.replaceAll(Matcher.quoteReplacement("$"), Matcher.quoteReplacement("\\$"));
		}
		this.static_wdc_only = static_wdc_only;
		this.total_edges_added = 0;
		this.total_iterations = 0;
		this.total_dynamic_instances = 1;
		this.min_distance = Long.MAX_VALUE;
		this.max_distance = Long.MIN_VALUE;
	}
	
	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.name.equals(((RaceInfo)o).name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public String getName() {
		return name;
	}
	
	public long getTotal_iterations() {
		return total_iterations;
	}
	
	public void addTotal_iterations(long new_iterations) {
		this.total_iterations += new_iterations;
	}
	
	public long getTotal_edges_added() {
		return total_edges_added;
	}
	
	public void addTotal_edges_added(long new_edges_added) {
		this.total_edges_added += new_edges_added;
	}

	public boolean isStatic_wdc_only() {
		return static_wdc_only;
	}

	public void setStatic_wdc_only(boolean static_wdc_only) {
		this.static_wdc_only = static_wdc_only;
	}

	public long getTotal_dynamic_instances() {
		return total_dynamic_instances;
	}

	public void incrementTotal_dynamic_instances() {
		this.total_dynamic_instances++;
	}

	public long getMax_distance() {
		return max_distance;
	}

	public long getMin_distance() {
		return min_distance;
	}

	public void set_distance(long distance) {
		if (distance < min_distance) {
			this.min_distance = distance;
		}
		if (distance > max_distance) {
			this.max_distance = distance;
		}
	}
	
	public void set_static_trial(int static_trial) {
		this.static_trial = static_trial;
	}
	
	public int get_static_trial() {
		return this.static_trial;
	}

	@Override
	public String toString() {
		return name+"&"+(total_iterations/total_dynamic_instances)+"&"+(total_edges_added/total_dynamic_instances)+"&("+getParenthesis(String.valueOf(min_distance))+"--"+getParenthesis(String.valueOf(max_distance))+")";
	}
	
	public String getParenthesis(String value) {
		char [] digits = value.toCharArray();
		String formatted_value = "";
		int parenthesis_counter = 0;
		for (int index = digits.length-1; index >= 0; index--) {
			formatted_value = digits[index] + formatted_value;
			parenthesis_counter++;
			if (parenthesis_counter == 3 && index != 0) {
				formatted_value = "," + formatted_value;
				parenthesis_counter = 0;
			}
			
		}
		return formatted_value;
	}
}
