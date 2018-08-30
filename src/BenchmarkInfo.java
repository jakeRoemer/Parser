import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;

public class BenchmarkInfo {
	final String benchmark;
	
	String event_total;
	double event_total_trials;
	
	String total_thread_count;
	int total_thread_count_trials;
	String max_live_thread_count;
	int max_live_thread_count_trials;
	
	LinkedList<String> config_total_time;
	HashMap<String, Double> config_total_time_trials;
	
	LinkedList<String> config_mem;
	HashMap<String, Double> config_mem_trials;
	
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
	
	public BenchmarkInfo(String benchmark, int total_trials) {
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
		
		this.event_total_trials = 0;
		this.total_thread_count_trials = 0;
		this.max_live_thread_count_trials = 0;
		this.config_total_time_trials = new HashMap<String, Double>();
		this.config_mem_trials = new HashMap<String, Double>();
		this.static_check_time_trials = 0;
		this.dynamic_check_time_trials = 0;
		this.total_trials = total_trials;
		
		this.counts = new HashMap<String, EventCounts>();
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

	public String getTotalThread_count() {
		if (total_thread_count == null) {
			this.total_thread_count = "\\newcommand{\\"+benchmark+"TotalThreads}{\\rna}\n";
		}
		return total_thread_count;
	}

	public void setTotalThread_count(String config, String thread_count, boolean final_trial) {
		if (config.equals("pipQ_dc")) config = "pip_dc";
		if (config.equals("wdc_exc") || config.equals("pip_dc")) {
			this.total_thread_count_trials += Integer.parseInt(thread_count);
			if (final_trial) {
				this.total_thread_count = "\\newcommand{\\"+benchmark+"TotalThreads}{"+(this.total_thread_count_trials/this.total_trials)+"}\n";
			}
		}
	}
	
	public String getMaxLiveThread_count() {
		if (max_live_thread_count == null) {
			this.max_live_thread_count = "\\newcommand{\\"+benchmark+"MaxLiveThreads}{\\rna}\n";
		}
		return max_live_thread_count;
	}

	public void setMaxLiveThread_count(String config, String thread_count, boolean final_trial) {
		if (config.equals("pipQ_dc")) config = "pip_dc";
		if (config.equals("wdc_exc") || config.equals("pip_dc")) {
			this.max_live_thread_count_trials += Integer.parseInt(thread_count);
			if (final_trial) {
				this.max_live_thread_count = "\\newcommand{\\"+benchmark+"MaxLiveThreads}{"+(this.max_live_thread_count_trials/this.total_trials)+"}\n";
			}
		}
	}

	public LinkedList<String> getConfig_total_time(String [] configs, String [] configNames) {
		for (int i = 0; i < configs.length; i++) {
			if (this.config_total_time_trials.get(configs[i]) == null) {
				this.config_total_time.add("\\newcommand{\\"+benchmark+configNames[i]+"Time}{\\rna}\n");
			} else {
				Double normalized_time = this.config_total_time_trials.get(configs[i])/this.total_trials;
				if (!configs[i].equals("base")) {
//					normalized_time = normalized_time / (this.config_total_time_trials.get("base")/this.total_trials);
					normalized_time = normalized_time / 1000;
					normalized_time = normalized_time / (this.config_total_time_trials.get("base")/1000/this.total_trials);
				}
				String config_total_time_string = String.valueOf(getTwoSigsRound(normalized_time));
				if (config_total_time_string.length() <= 1) {
					config_total_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
				}
				if (configs[i].equals("base")) {
					if (config_total_time_string.length() > 4) {
						config_total_time_string = getParenthesis(config_total_time_string.substring(0, config_total_time_string.length()-3));
					} else {
						config_total_time_string =  getDecimals(config_total_time_string);
					}
				} else if (config_total_time_string.length() >= 4 && !config_total_time_string.contains(".")) {
					config_total_time_string = getParenthesis(config_total_time_string);
				}
				this.config_total_time.add("\\newcommand{\\"+benchmark+configNames[i]+"Time}{"+config_total_time_string+"}\n");
			}
		}
		return config_total_time;
	}

	public void setConfig_total_time(String configName, String config, String total_time, String bench_time, boolean final_trial) {
		double time = Double.parseDouble(total_time);
		if (this.config_total_time_trials.containsKey(config)) {
			time += this.config_total_time_trials.get(config);
		}
		this.config_total_time_trials.put(config, time);
	}

	public LinkedList<String> getConfig_mem() {
		return this.config_mem;
	}

	public void setConfig_mem(String configName, String memory, boolean final_trial) {
		double time = Double.parseDouble(memory)/1000; //From MBs to GBs
		if (this.config_mem_trials.containsKey(configName)) {
			time += this.config_mem_trials.get(configName);
		}
		this.config_mem_trials.put(configName, time);
		if (final_trial) {
			long avg_mem = getTwoSigsRound(this.config_mem_trials.get(configName)/this.total_trials);
			String config_mem_string = String.valueOf(avg_mem);
			if (avg_mem/1000.0 <= 2.0) {
				config_mem_string = "\\memna";
			} else {
				config_mem_string = config_mem_string.length() > 4 ? getParenthesis(config_mem_string.substring(0, config_mem_string.length()-3)) : getDecimals(config_mem_string);
			}
			this.config_mem.add("\\newcommand{\\"+benchmark+configName+"Mem}{"+config_mem_string+"}\n");
		}
	}

	public LinkedList<String> getConfig_bench_time() {
		return config_bench_time;
	}

	public void setConfig_bench_time(String configName, String bench_time) {
		this.config_bench_time.add("\\newcommand{\\"+benchmark+configName+"Bench}{"+bench_time+"}\n");
	}

	public LinkedList<String> getRace_types(String tool) {
		if (tool.equals("DC")) {
			String[] types = {"HB", "HBDynamic", "PIPHB", "PIPHBDynamic", "FT", "FTDynamic", "WCP", "WCPDynamic", "PIPWCP", "PIPWCPDynamic", "WDC", "WDCDynamic", "PIPWDC", "PIPWDCDynamic", "CAPO", "CAPODynamic", "PIPCAPO", "PIPCAPODynamic", "PIP", "PIPDynamic", "PIPPIP", "PIPPIPDynamic"};
			for (String type : types) {
				if (this.types.get(type) == null) {
					this.race_types.add("\\newcommand{\\"+benchmark+type+"}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CI}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMIN}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMAX}{\\rna}\n");
				} else {
					long avgRaces = round(EventCounts.getAvg(this.types.get(type)));
					String type_races_string = String.valueOf(avgRaces);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"}{"+getParenthesis(type_races_string)+"}\n");
					//Add confidence intervals
					long ci = round(EventCounts.calcCI(this.types.get(type)));
					String ciString = String.valueOf(ci);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CI}{"+getParenthesis(ciString)+"}\n");
					//Applying confidence intervals to data set
					String minRaces = String.valueOf(avgRaces - ci);
					String maxRaces = String.valueOf(avgRaces + ci);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMIN}{"+getParenthesis(minRaces)+"}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMAX}{"+getParenthesis(maxRaces)+"}\n");
				}
			}
		}
		if (tool.equals("PIP")) {
			String[] types = {"HB", "HBDynamic", "FT", "FTDynamic", "WCP", "WCPDynamic", "WDC", "WDCDynamic", "CAPO", "CAPODynamic", "CAPOOPT", "CAPOOPTDynamic", "CAPOOPTALT", "CAPOOPTALTDynamic"};//, "PIP", "PIPDynamic"};
			for (String type : types) {
				if (this.types.get(type) == null) {
					this.race_types.add("\\newcommand{\\"+benchmark+type+"}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CI}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMIN}{\\rna}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMAX}{\\rna}\n");
				} else {
					long avgRaces = round(EventCounts.getAvg(this.types.get(type)));
					String type_races_string = String.valueOf(avgRaces);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"}{"+getParenthesis(type_races_string)+"}\n");
					//Add confidence intervals
					long ci = round(EventCounts.calcCI(this.types.get(type)));
					String ciString = String.valueOf(ci);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CI}{"+getParenthesis(ciString)+"}\n");
					//Applying confidence intervals to data set
					String minRaces = String.valueOf(avgRaces - ci);
					String maxRaces = String.valueOf(avgRaces + ci);
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMIN}{"+getParenthesis(minRaces)+"}\n");
					this.race_types.add("\\newcommand{\\"+benchmark+type+"CIMAX}{"+getParenthesis(maxRaces)+"}\n");
				}
			}
		}
		return race_types;
	}
	
	public LinkedList<String> getCapoRace_types() {
		return capo_race_types;
	}
	
	public LinkedList<String> getPIPRace_types() {
		return pip_race_types;
	}

	public void setRace_types(String config, String type, String race_num, String tool, int curr_trial, int total_trials) {
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
//		System.out.println(String.format("setting config, type: %s, %s | race_num: %s", config, type, race_num));
		//If this is the last trial and there were failed trials, resize the data set with only successful trials
		if (curr_trial == total_trials) {
			int failures = EventCounts.failedTrials(type_races);
//			System.out.println(String.format("config: %s bench: %s trial: %d failures %d", config, benchmark, curr_trial, failures));
			if (failures > 0) type_races = EventCounts.resize(type_races, type_races.length-failures);
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

	public String getStatic_check_time() {
		if (this.static_check_time_trials == 0) {
			this.static_check_time = "\\newcommand{\\"+benchmark+"StaticTime}{\\rzero}\n";
		} else {
			Double normalized_time = this.static_check_time_trials/this.total_trials;
			normalized_time = normalized_time / 1000;
			
			String static_check_time_string = String.valueOf(getTwoSigsRound(normalized_time));
			if (static_check_time_string.length() <= 1) {
				static_check_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
			} else if (static_check_time_string.length() >= 4) {
				static_check_time_string = getParenthesis(static_check_time_string);
			}
			this.static_check_time = "\\newcommand{\\"+benchmark+"StaticTime}{"+(static_check_time_string.equals("0.000") ? "\\rzero" : static_check_time_string)+"}\n";
		}
		return static_check_time;
	}

	public void setStatic_check_time(String config, String static_check_time, boolean final_trial) {
		if (config.equals("wdc_exc")) {
			this.static_check_time_trials += Double.parseDouble(static_check_time);
			double config_time = this.config_total_time_trials.get("wdc_exc");
			config_time += Double.parseDouble(static_check_time);
			this.config_total_time_trials.put("wdc_exc", config_time);
		}
	}

	public String getDynamic_check_time() {
		if (this.dynamic_check_time_trials == 0) {
			this.dynamic_check_time = "\\newcommand{\\"+benchmark+"DynamicTime}{\\rzero}\n";
		} else {
			Double normalized_time = this.dynamic_check_time_trials/this.total_trials;
//			normalized_time = normalized_time / (this.config_total_time_trials.get("base")/this.total_trials);
			normalized_time = normalized_time / 1000;
			String dynamic_check_time_string = String.valueOf(getTwoSigsRound(normalized_time));
			if (dynamic_check_time_string.length() <= 1) {
				dynamic_check_time_string = String.valueOf(getTwoSigsDouble(normalized_time));
			} else if (dynamic_check_time_string.length() >= 4) {
				dynamic_check_time_string = getParenthesis(dynamic_check_time_string);
			}
			this.dynamic_check_time = "\\newcommand{\\"+benchmark+"DynamicTime}{"+(dynamic_check_time_string.equals("0.000") ? "\\rzero" : dynamic_check_time_string)+"}\n";
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
	
	//TODO: clean this up
	public String getCount_Total(String tool, String totalType) {
		String config = "";
		if (tool.equals("DC")) {
			config = "wdc_exc";
		} else { //PIP tool
			config = "pip_dc";
		}
		String event_total_string = "";
		if (config.equals("pip_dc")) {
			if (totalType.equals("Events")) {
				if (counts.get(config) == null) { //fail state
					event_total_string = "0";
				} else {
					event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal()));
				}
				if (event_total_string.equals("0")) {
					if (counts.get(config) != null) {
						event_total_string = String.valueOf(getTwoSigsRound(EventCounts.add(counts.get(config).getTotal_ops(), counts.get(config).getTotal_fast_path_taken())));//counts.get(config).getTotal_ops() + counts.get(config).getTotal_fast_path_taken()));
					}
				}
			} else if (totalType.equals("NoFPEvents")) {
				if (counts.get(config) == null) { //fail state
					event_total_string = "0";
				} else {
					event_total_string = String.valueOf(getTwoSigsRound(EventCounts.add(EventCounts.add(counts.get(config).getTotal(), counts.get(config).getFp_write()), counts.get(config).getFp_read())));//counts.get(config).getTotal() + counts.get(config).getFp_write() + counts.get(config).getFp_read()));
				}
				if (event_total_string.equals("0")) {
					if (counts.get(config) != null) {
						event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal_ops()));
					}
				}
			}
		} else { //For wdc_exc
			if (totalType.equals("Events")) {
				if (counts.get(config) == null) { //fail state
					event_total_string = "0";
				} else {
					event_total_string = String.valueOf(getTwoSigsRound(EventCounts.add(EventCounts.add(counts.get(config).getTotal(), counts.get(config).getFp_write()), counts.get(config).getFp_read())));//counts.get(config).getTotal() + counts.get(config).getFp_write() + counts.get(config).getFp_read()));
				}
				if (event_total_string.equals("0")) {
					if (counts.get(config) != null) {
						event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal_ops()));
					}
				}
			} else if (totalType.equals("NoFPEvents")) {
				if (counts.get(config) == null) { //fail state
					event_total_string = "0";
				} else {
					event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal()));
				}
				if (event_total_string.equals("0")) {
					if (counts.get(config) != null) {
						event_total_string = String.valueOf(getTwoSigsRound(EventCounts.sub(counts.get(config).getTotal_ops(), counts.get(config).getTotal_fast_path_taken())));//counts.get(config).getTotal_ops() - counts.get(config).getTotal_fast_path_taken()));
					}
				}
			} else if (totalType.equals("Ops")) {
				event_total_string = String.valueOf(getTwoSigsRound(counts.get(config).getTotal()));
			}
		}
		if (event_total_string.isEmpty()) {
			this.event_total = "\\newcommand{\\"+benchmark+totalType+"}{\\rna}\n";
		} else {
			if (!event_total_string.equals("0")) {
				event_total_string = event_total_string.substring(0, event_total_string.length()-3);
				event_total_string = event_total_string.length() > 4 ? getParenthesis(event_total_string.substring(0, event_total_string.length()-3)) : getDecimals(event_total_string);
			}
			this.event_total = "\\newcommand{\\"+benchmark+totalType+"}{"+event_total_string+"}\n";
		}
		
		return event_total;
	}

	public void setCounts(String config, String eventType, long eventCount, int curr_trial) {
//		System.out.println("config: " + config + " | event type: " + eventType + " | event count: " + eventCount);
		EventCounts configCounts = counts.get(config);
		if (configCounts == null) {
			configCounts = new EventCounts(config, benchmark);
		}
		configCounts.setEventCounts(eventType, eventCount, curr_trial, this.total_trials);
		counts.put(config, configCounts);
	}
}
