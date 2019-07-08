import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;

public class BenchmarkInfo {
	final String tool;
	final String benchmark;
	
	String event_total;
	double event_total_trials;
	
	HashMap<String, int[]> total_thread_count;
	HashMap<String, int[]> max_live_thread_count;
	
	LinkedList<String> config_total_time;
	HashMap<String, double[]> config_total_time_trials;
	
	LinkedList<String> config_mem;
	HashMap<String, double[]> config_mem_trials;
	
	LinkedList<String> config_bench_time;
	
	LinkedList<String> race_types;
	HashMap<String, double[]> types;
	HashMap<String, RaceInfo> races;
	RaceInfo latest_race;
	
	LinkedList<String> capo_race_types;
	HashMap<String, Double> capo_types;
	
	LinkedList<String> pip_race_types;
	HashMap<String, Double> pip_types;
	
	String static_check_time;
	double static_check_time_trials;
	
	String dynamic_check_time;
	double dynamic_check_time_trials;
	
	int total_trials;
	
	HashMap<String, EventCounts> counts;
	
	public BenchmarkInfo(String tool, String benchmark, int total_trials) {
		this.tool = tool.equals("DC") ? "SLOW" : "FAST";
		this.benchmark = benchmark;
		this.config_total_time = new LinkedList<String>();
		this.config_mem = new LinkedList<String>();
		this.config_bench_time = new LinkedList<String>();
		this.race_types = new LinkedList<String>();
		this.types = new HashMap<String, double[]>();
		this.races = new HashMap<String, RaceInfo>();
		
		this.capo_race_types = new LinkedList<String>();
		this.capo_types = new HashMap<String, Double>();
		
		this.pip_race_types = new LinkedList<String>();
		this.pip_types = new HashMap<String, Double>();
		
		this.total_thread_count = new HashMap<String, int[]>();
		this.max_live_thread_count = new HashMap<String, int[]>();
		
		this.event_total_trials = 0;
		this.config_total_time_trials = new HashMap<String, double[]>();
		this.config_mem_trials = new HashMap<String, double[]>();
		this.static_check_time_trials = 0;
		this.dynamic_check_time_trials = 0;
		this.total_trials = total_trials;
		
		this.counts = new HashMap<String, EventCounts>();
	}
	
	public String getTool() {
		return tool;
	}
	public String getBenchmark() {
		return benchmark;
	}
	
	public static long round(double value) {
		if ((value - Math.floor(value)) >= 0.5) {
			value = Math.floor(value) + 1;
		} else {
			value = Math.floor(value);
		}
		return (long)value;
	}
	
	public static String getDecimals(String value) {
		if (value.length() == 4) {
			value = value.charAt(0) + "." + value.charAt(1);
		} else if (value.length() == 3) {
			value = "0." + value.charAt(0) + value.charAt(1);
		} else if (value.length() == 2) {
			value = "0.0" + value.charAt(0) + value.charAt(1);
		} else if (value.length() == 1) {
			value = "0.00" + value.charAt(0);
		}
		return value;
	}
	
	public static String getParenthesis(String value) {
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
	
	public static long getTwoSigsRound(double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(2));
		return bd.longValue();
	}
	
	public static long getTwoSigsRound(long[] val) {
		double value = 0;
		if (!EventCounts.isZero(val)) {
			value = EventCounts.getAvg(val);
		}
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(2));
		return bd.longValue();
	}
	
	public static double getTwoSigsDouble(double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(2));
		return bd.doubleValue();
	}
	
	public static double getThreeSigsDouble(double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3));
		return bd.doubleValue();
	}
	
	public String getTwoSigs(String value) {
		String old_value = value; //if needed
		if (value.length() <= 3) {
			if (value.length() == 3) {
				value = "0." + old_value;
			} else if (value.length() == 2){
				value = "0.0" + old_value;
			} else if (value.length() == 1) {
				value = "0.00" + old_value;
			}
			return value;
		}
		value = value.substring(0, value.length()-3);
		if (value.length() < 2) {
			value = value + "." + old_value.substring(1, 2);
			return value;
		} else if (value.length() == 2) {
			return value;
		} else {
			int comma_injection = 0;
			String newValue = "";
			for (int i = 0; i < value.length()-2; i++) {
				comma_injection++;
				newValue = "0" + newValue;
				if (comma_injection == 3) {
					newValue = "," + newValue;
					comma_injection = 0;
				}
			}
			newValue = value.charAt(1) + newValue;
			comma_injection++;
			if (comma_injection == 3) {
				newValue = "," + newValue;
			}
			newValue = value.charAt(0) + newValue;
			return newValue;
		}
	}
	
	public static long getThreeSigsRound(double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.round(new MathContext(3));
		return bd.longValue();
	}
	
	public String getThreeSigs(String value) {
		if (value.length() <= 3) {
			return value;
		} else {
			int comma_injection = 0;
			String newValue = "";
			for (int i = 0; i < value.length()-3; i++) {
				comma_injection++;
				newValue = "0" + newValue;
				if (comma_injection == 3) {
					newValue = "," + newValue;
					comma_injection = 0;
				}
			}
			newValue = value.charAt(2) + newValue;
			comma_injection++;
			if (comma_injection == 3) {
				newValue = "," + newValue;
			}
			newValue = value.charAt(1) + newValue;
			comma_injection++;
			if (comma_injection == 3) {
				newValue = "," + newValue;
			}
			newValue = value.charAt(0) + newValue;
			return newValue;
		}
	}

	public String getTotalThread_count(String config) {
		int[] thread_counts = this.total_thread_count.get(config);
		if (thread_counts == null) {
			return "\\newcommand{\\"+getTool()+benchmark+"TotalThreads}{\\rna}\n";
		}
		return "\\newcommand{\\"+getTool()+benchmark+"TotalThreads}{"+EventCounts.getAvg(thread_counts)+"}\n";
	}

	public void setTotalThread_count(String config, String thread_count, int curr_trial, int total_trials) {			
		int[] thread_counts = this.total_thread_count.get(config);
		if (thread_counts == null) {
			thread_counts = new int[total_trials];
			for (int i = 0; i < thread_counts.length; i++) thread_counts[i] = -1; //failed trial identifier
			this.total_thread_count.put(config, thread_counts);
		}
		thread_counts[curr_trial-1] = Integer.parseInt(thread_count); //trial counts start at 1
		//If this is the last trial and there were failed trials, resize the data set with only successful trials
		if (curr_trial == total_trials) {
			int failures = EventCounts.failedTrials(thread_counts);
			if (failures > 0) thread_counts = EventCounts.resize(thread_counts, thread_counts.length-failures);
		}
	}
	
	public String getMaxLiveThread_count(String config) {
		int[] thread_counts = this.max_live_thread_count.get(config);
		if (thread_counts == null) {
			return "\\newcommand{\\"+getTool()+benchmark+"MaxLiveThreads}{\\rna}\n";
		}
		return "\\newcommand{\\"+getTool()+benchmark+"MaxLiveThreads}{"+EventCounts.getAvg(thread_counts)+"}\n";
	}

	public void setMaxLiveThread_count(String config, String thread_count, int curr_trial, int total_trials) {
		int[] thread_counts = this.max_live_thread_count.get(config);
		if (thread_counts == null) {
			thread_counts = new int[total_trials];
			for (int i = 0; i < thread_counts.length; i++) thread_counts[i] = -1; //failed trial identifier
			this.max_live_thread_count.put(config, thread_counts);
		}
		thread_counts[curr_trial-1] = Integer.parseInt(thread_count); //trial counts start at 1
		//If this is the last trial and there were failed trials, resize the data set with only successful trials
		if (curr_trial == total_trials) {
			int failures = EventCounts.failedTrials(thread_counts);
			if (failures > 0) thread_counts = EventCounts.resize(thread_counts, thread_counts.length-failures);
		}
	}

	public LinkedList<String> getConfig_total_time(String[] configNames, HashMap<String, double[]> geoMeanTime, int benchIndex) {
		for (int i = 0; i < configNames.length; i++) {
			double normalized_time = -1;
			if (this.config_total_time_trials.get(configNames[i]) == null) {
				this.config_total_time.add("\\newcommand{\\"+getTool()+benchmark+configNames[i]+"Time}{\\rna}\n");
				this.config_total_time.add("\\newcommand{\\"+getTool()+benchmark+configNames[i]+"TimeCI}{\\rna}\n");
			} else {
				double[] config_total_time_trial = this.config_total_time_trials.get(configNames[i]);
				int failures = EventCounts.failedTrials(config_total_time_trial);
				if (failures > 0) config_total_time_trial = EventCounts.resize(config_total_time_trial, config_total_time_trial.length - failures);
				normalized_time = EventCounts.getAvg(config_total_time_trial);
				if (!configNames[i].equals("Base")) {
					normalized_time = normalized_time / (double)1000;
					normalized_time = normalized_time / (EventCounts.getAvg(this.config_total_time_trials.get("Base"))/(double)1000);
				}
				String config_total_time_string = String.valueOf(getTwoSigsRound(normalized_time));
				if (config_total_time_string.length() <= 1) {
					config_total_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
				}
				if (configNames[i].equals("Base")) {
					if (config_total_time_string.length() > 4) {
						config_total_time_string = getParenthesis(config_total_time_string.substring(0, config_total_time_string.length()-3));
					} else {
						config_total_time_string = getDecimals(config_total_time_string);
					}
				} else if (config_total_time_string.length() >= 4 && !config_total_time_string.contains(".")) {
					config_total_time_string = getParenthesis(config_total_time_string);
				}
				if (config_total_time_string.length() > 3 && config_total_time_string.substring(config_total_time_string.length()-2).equals(".0")) {
					config_total_time_string = config_total_time_string.substring(0, config_total_time_string.length()-2);
				}
				this.config_total_time.add("\\newcommand{\\"+getTool()+benchmark+configNames[i]+"Time}{"+config_total_time_string+"}\n");
				//Add confidence intervals
				if (!configNames[i].equals("Base")) {
					for (int k = 0; k < this.config_total_time_trials.get(configNames[i]).length; k++) {
						this.config_total_time_trials.get(configNames[i])[k] = (this.config_total_time_trials.get(configNames[i])[k] / (double)1000) / (this.config_total_time_trials.get("Base")[k] / (double)1000);
					}
				}
				double ci = EventCounts.calcCI(this.config_total_time_trials.get(configNames[i]));
				String ciString = String.valueOf(getTwoSigsRound(ci));
				if (ciString.length() <= 1) ciString = String.valueOf(getTwoSigsDouble(ci));
				if (ciString.length() >= 4 && !ciString.contains(".")) ciString = getParenthesis(ciString);
				this.config_total_time.add("\\newcommand{\\"+getTool()+benchmark+configNames[i]+"TimeCI}{"+ciString+"}\n");
			}
			geoMeanTime.get(configNames[i])[benchIndex] = normalized_time;
		}
		return config_total_time;
	}

	public void setConfig_total_time(String configName, String total_time, String bench_time, int curr_trial, int total_trials) {
		double[] config_total_time_trial = this.config_total_time_trials.get(configName);
		if (config_total_time_trial == null) {
			config_total_time_trial = new double[total_trials];
			for (int i = 0; i < config_total_time_trial.length; i++) {
				config_total_time_trial[i] = -1; //failed trial identifier
			}
		}
		double time = Double.parseDouble(total_time);
		config_total_time_trial[curr_trial-1] = time;
		this.config_total_time_trials.put(configName, config_total_time_trial);
	}

	public LinkedList<String> getConfig_mem(String tool, String[] types, HashMap<String, double[]> geoMeanMem, int benchIndex) {
		for (String type : types) {
			double normalized_mem = -1;
			if (!this.config_mem_trials.containsKey(type)) {
				this.config_mem.add("\\newcommand{\\"+getTool()+benchmark+type+"Mem}{\\memna}\n");
				this.config_mem.add("\\newcommand{\\"+getTool()+benchmark+type+"MemCI}{\\memna}\n");
			} else {
				double[] config_mem_trial = this.config_mem_trials.get(type);
				int failures = EventCounts.failedTrials(config_mem_trial);
				if (failures > 0) config_mem_trial = EventCounts.resize(config_mem_trial, config_mem_trial.length - failures);
				normalized_mem = getTwoSigsDouble(EventCounts.getAvg(config_mem_trial));
				if (!type.equals("Base")) {
					double[] base_mem_trial = this.config_mem_trials.get("Base");
					int base_failures = EventCounts.failedTrials(base_mem_trial);
					if (base_failures > 0) base_mem_trial = EventCounts.resize(base_mem_trial, base_mem_trial.length - base_failures);
					normalized_mem = normalized_mem / EventCounts.getAvg(base_mem_trial);
				}
				String config_mem_string = String.valueOf(getTwoSigsRound(normalized_mem));
				if (config_mem_string.length() <= 1) {
					config_mem_string = String.valueOf(getTwoSigsDouble(normalized_mem));
				} else if (config_mem_string.length() >= 4 && !config_mem_string.contains(".")) {
					config_mem_string = getParenthesis(config_mem_string);
				}
				if (config_mem_string.length() > 3 && config_mem_string.substring(config_mem_string.length()-2).equals(".0")) {
					config_mem_string = config_mem_string.substring(0, config_mem_string.length()-2);
				}
				this.config_mem.add("\\newcommand{\\"+getTool()+benchmark+type+"Mem}{"+config_mem_string+"}\n");
				//Add confidence intervals
				if (!type.equals("Base")) {
					for (int i = 0; i < this.config_mem_trials.get(type).length; i++) {
						this.config_mem_trials.get(type)[i] = (this.config_mem_trials.get(type)[i]) / (this.config_mem_trials.get("Base")[i]);
					}
				}
				double ci = EventCounts.calcCI(this.config_mem_trials.get(type));
				String ciString = String.valueOf(getTwoSigsDouble(ci));
				this.config_mem.add("\\newcommand{\\"+getTool()+benchmark+type+"MemCI}{"+ciString+"}\n");
			}
			geoMeanMem.get(type)[benchIndex] = normalized_mem; //Mb
		}
		return this.config_mem;
	}

	public void setConfig_mem(String configName, String memory, int curr_trial, int total_trials) {
		double[] config_mem_trial = this.config_mem_trials.get(configName);
		if (config_mem_trial == null) {
			config_mem_trial = new double[total_trials];
			for (int i = 0; i < config_mem_trial.length; i++) config_mem_trial[i] = -1; //failed trial identifier
		}
		double mem = Double.parseDouble(memory)/1000; //From kBs to MBs
		config_mem_trial[curr_trial-1] = mem;
		this.config_mem_trials.put(configName, config_mem_trial);
	}

	public LinkedList<String> getConfig_bench_time() {
		return config_bench_time;
	}

	public void setConfig_bench_time(String configName, String bench_time) {
		this.config_bench_time.add("\\newcommand{\\"+getTool()+benchmark+configName+"Bench}{"+bench_time+"}\n");
	}

	public LinkedList<String> getRace_types(String tool, String[] types, HashMap<String, double[]> totalRace, int benchIndex, int total_benchmarks) {
		if (totalRace.isEmpty()) { 
			for (String type : types) {
				totalRace.put(type, new double[total_benchmarks]);
			}
		}
		for (String type : types) {
			long avgRaces = 0;
			if (this.types.get(type) == null) {
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"}{\\rna}\n");
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CI}{\\rna}\n");
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CIMIN}{\\rna}\n");
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CIMAX}{\\rna}\n");
			} else {
				double[] type_races = this.types.get(type);
				int failures = EventCounts.failedTrials(type_races);
				if (failures > 0) type_races = EventCounts.resize(type_races, type_races.length-failures);
				avgRaces = round(EventCounts.getAvg(type_races));				
				String type_races_string = String.valueOf(avgRaces);
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"}{"+getParenthesis(type_races_string)+"}\n");
				//Add confidence intervals
				long ci = round(EventCounts.calcCI(this.types.get(type)));
				String ciString = String.valueOf(ci);
				if (ciString.length() == 1) {
					double ciDouble = getTwoSigsDouble(EventCounts.calcCI(this.types.get(type)));
					ciString = String.valueOf(ciDouble);
					this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CI}{"+ciString+"}\n");
				} else {
					this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CI}{"+getParenthesis(ciString)+"}\n");
				}
				//Applying confidence intervals to data set
				String minRaces = String.valueOf(avgRaces - ci);
				String maxRaces = String.valueOf(avgRaces + ci);
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CIMIN}{"+getParenthesis(minRaces)+"}\n");
				this.race_types.add("\\newcommand{\\"+getTool()+benchmark+type+"CIMAX}{"+getParenthesis(maxRaces)+"}\n");
			}
			totalRace.get(type)[benchIndex] = avgRaces;
		}
		return race_types;
	}

	public void setRace_types(String type, String race_num, int curr_trial, int total_trials) {
		double[] type_races = this.types.get(type);
		if (type_races == null) {
			type_races = new double[total_trials];
			for (int i = 0; i < type_races.length; i++) type_races[i] = -1; //failed trial identifier
		}
		if (type_races[curr_trial-1] != -1) {
			type_races[curr_trial-1] += Double.parseDouble(race_num);
		} else {
			type_races[curr_trial-1] = Double.parseDouble(race_num);
		}
		this.types.put(type, type_races);
	}

	public HashMap<String, RaceInfo> getRaces() {
		return races;
	}

	public void setRaces() {
		if (this.latest_race != null) {
			RaceInfo adding_race = this.latest_race;
			races.put(this.latest_race.getFull_name(), adding_race);
		}
	}

	public RaceInfo getLatest_race() {
		return latest_race;
	}

	public void setLatest_race(String race_name, boolean wdc_static_only) {
		this.latest_race = races.get(race_name);
		if (this.latest_race == null) {
			this.latest_race = new RaceInfo(race_name, wdc_static_only, benchmark.equals("tomcat") ? true : false);
		} else {
			this.latest_race.incrementTotal_dynamic_instances();
		}
	}
	
	public LinkedList<String> getCapoRace_types() {
		return capo_race_types;
	}
	
	public LinkedList<String> getPIPRace_types() {
		return pip_race_types;
	}

	public String getStatic_check_time() {
		if (this.static_check_time_trials == 0) {
			this.static_check_time = "\\newcommand{\\"+getTool()+benchmark+"StaticTime}{\\rzero}\n";
		} else {
			Double normalized_time = this.static_check_time_trials/this.total_trials;
			normalized_time = normalized_time / 1000;
			
			String static_check_time_string = String.valueOf(getTwoSigsRound(normalized_time));
			if (static_check_time_string.length() <= 1) {
				static_check_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
			} else if (static_check_time_string.length() >= 4) {
				static_check_time_string = getParenthesis(static_check_time_string);
			}
			this.static_check_time = "\\newcommand{\\"+getTool()+benchmark+"StaticTime}{"+(static_check_time_string.equals("0.000") ? "\\rzero" : static_check_time_string)+"}\n";
		}
		return static_check_time;
	}

	public void setStatic_check_time(String config, String static_check_time, boolean final_trial) {
		if (config.equals("wdc_exc")) {
			this.static_check_time_trials += Double.parseDouble(static_check_time);
		}
	}

	public String getDynamic_check_time() {
		if (this.dynamic_check_time_trials == 0) {
			this.dynamic_check_time = "\\newcommand{\\"+getTool()+benchmark+"DynamicTime}{\\rzero}\n";
		} else {
			Double normalized_time = this.dynamic_check_time_trials/this.total_trials;
			normalized_time = normalized_time / 1000;
			String dynamic_check_time_string = String.valueOf(getTwoSigsRound(normalized_time));
			if (dynamic_check_time_string.length() <= 1) {
				dynamic_check_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
			} else if (dynamic_check_time_string.length() >= 4) {
				dynamic_check_time_string = getParenthesis(dynamic_check_time_string);
			}
			this.dynamic_check_time = "\\newcommand{\\"+getTool()+benchmark+"DynamicTime}{"+(dynamic_check_time_string.equals("0.000") ? "\\rzero" : dynamic_check_time_string)+"}\n";
		}
		return dynamic_check_time;
	}

	public void setDynamic_check_time(String config, String dynamic_check_time, boolean final_trial) {
		if (config.equals("wdc_exc")) {
			this.dynamic_check_time_trials += Double.parseDouble(dynamic_check_time);
		}
	}

	public HashMap<String, EventCounts> getCounts() {
		return counts;
	}
	
	public String getCount_Total(String tool, String config, String totalType) {
		String event_total_string = "0";
		long avgEvents = 0;
		long ci = 0;
		if (counts.get(config) != null) {
			if (totalType.equals("Events")) {
				event_total_string = String.valueOf(getTwoSigsRound(EventCounts.add(counts.get(config).getTotal_ops(), counts.get(config).getTotal_fast_path_taken())));
				avgEvents = round(EventCounts.getAvg(EventCounts.add(counts.get(config).getTotal_ops(), counts.get(config).getTotal_fast_path_taken())));
				ci = round(EventCounts.calcCI(EventCounts.add(counts.get(config).getTotal_ops(), counts.get(config).getTotal_fast_path_taken())));
			} else if (totalType.equals("NoFPEvents")) {
				event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal_ops()));
				avgEvents = round(EventCounts.getAvg(counts.get(config).getTotal_ops()));
				ci = round(EventCounts.calcCI(counts.get(config).getTotal_ops()));
			}
			//Add confidence intervals
			String ciString = String.valueOf(ci);
			this.race_types.add("\\newcommand{\\"+getTool()+benchmark+totalType+"CI}{"+getParenthesis(ciString)+"}\n");
			//Applying confidence intervals to data set
			String minRaces = String.valueOf(avgEvents - ci);
			String maxRaces = String.valueOf(avgEvents + ci);
			this.race_types.add("\\newcommand{\\"+getTool()+benchmark+totalType+"CIMIN}{"+getParenthesis(minRaces)+"}\n");
			this.race_types.add("\\newcommand{\\"+getTool()+benchmark+totalType+"CIMAX}{"+getParenthesis(maxRaces)+"}\n");
		}
			
		if (event_total_string.isEmpty()) {
			this.event_total = "\\newcommand{\\"+getTool()+benchmark+totalType+"}{\\rna}\n";
		} else {
			if (!event_total_string.equals("0")) {
				event_total_string = event_total_string.substring(0, event_total_string.length()-3);
				event_total_string = event_total_string.length() > 4 ? getParenthesis(event_total_string.substring(0, event_total_string.length()-3)) : getDecimals(event_total_string);
			}
			this.event_total = "\\newcommand{\\"+getTool()+benchmark+totalType+"}{"+event_total_string+"}\n";
		}
		
		return event_total;
	}

	public void setCounts(String config, String eventType, long eventCount, int curr_trial) {
		EventCounts configCounts = counts.get(config);
		if (configCounts == null) {
			configCounts = new EventCounts(config, benchmark);
		}
		configCounts.setEventCounts(eventType, eventCount, curr_trial, this.total_trials);
		counts.put(config, configCounts);
	}
}
