import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class parseDC {
	static final String[] benchmarks = {"avrora", "batik", "htwo", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "xalan"};
	static final int trials = 5; //Integer.parseInt(args[1]);
	static final String tool = "PIP"; //DC or PIP
	static final boolean fieldRace = false; //true = field, false = single second site
	static final boolean extraStats = true; //false for DC tool and false if counts are not collected.
	static final String output_dir = /*args[0];*/ "PIP_fastTool_sunflowCheck";
	
	//TODO: Change everything WDC to DC
	public static void main (String [] args) {
		//Vindicator tool //configs -> wdc_testconfig | configNames = DCLite
//		String [] configs = {"base", "empty", "hbwcp", "wdc_noG", "wdc"};//, "capo_only", "pip_only"};//, "wdc_noG", "capo_noG", "pip_noG"};
//		String [] configNames = {"Base", "Empty", "HBWCP", "DCLite", "WDC"};//, "CAPO", "PIP"};// ,"DCLite", "CAPOLite", "PIPLite"};
		//FTO and RE PIP tool (fast tool)
		String [] configs = {"base", "empty", "ft", "pip_hb", "pip_fto_hb", "pip_wcp", "pip_fto_wcp", "pip_fto_re_wcp", "pip_dc", "pip_fto_dc", "pip_fto_re_dc", "pip_capo", "pip_fto_capo", "pip_fto_re_capo", "pip_agg"};
		String [] configNames = {"Base", "Empty", "FT", "HB", "FTOHB", "WCP", "FTOWCP", "REWCP", "DC", "FTODC", "REDC", "CAPO", "FTOCAPO", "RECAPO", "AGGCAPO"};
		//(Slow tool) PIP tool
//		String [] configs = {"base", "empty", "ft", "hb", "hbwcp", "wdc_noG_exc", "wdc_noG", "wdc_exc", "wdc", "capo_noG_exc", "capo_noG", "capo_exc", "capo"};//, "wdc_noG", "wdc_exc", "capo"};//, "wdc_exc", "capo_exc", "pip_exc", "pip"};
//		String [] configNames = {"Base", "Empty", "FT", "HB", "WCP", "WDCnoGExc", "WDCnoG", "WDCExc", "WDC", "CAPOnoGExc", "CAPOnoG", "CAPOExc", "CAPO"};//, "WDCLite", "WDCExc", "CAPOFull"};//, "DCExc", "CAPOExc", "PIPExc", "PIP"};
		//(Alternate hb configurations for slow tool) PIP tool 
//		String [] configs = {"base", "ft", "hb", "hb_g", "hb_raceedge", "hb_noG_raceedge"};
//		String [] configNames = {"Base", "FT", "HB", "HBwG", "HBwRaceEdge", "HBnoGwRaceEdge"};
		LinkedList<BenchmarkInfo> benchmarks_info = new LinkedList<BenchmarkInfo>();
		BufferedReader input = null;
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/git/PIP-paper/result-macros/" + output_dir + ".tex"));
			String line;
			String bench_time = "";
			System.out.println("Parsing result files...");
			LinkedList<String> raceIdentifierDC = new LinkedList<String>(Arrays.asList("HB", "wG", "wRaceEdge", "noGwRaceEdge", "WCP", /*"WDCnoGExc", "WDCnoG", "WDCExc",*/ "WDC", /*"CAPOnoGExc", "CAPOnoG", "CAPOExc",*/ "CAPO", /*"PIP"*/ "CAPOOPT", "CAPOOPTALT", "CAPORE", "CAPOREALT", "CAPOREOPT"));
			LinkedList<String> raceIdentifierPIP = new LinkedList<String>(Arrays.asList("PIP", "FastTrack"));
			LinkedList<String> raceIdentifier = (tool.equals("DC") ? raceIdentifierDC : raceIdentifierPIP);
			LinkedList<String> raceTypeTotal = new LinkedList<String>(Arrays.asList("HB", "FTOHB", "WCP", "FTOWCP", "REWCP", "DC", "FTODC", "REDC", "CAPO", "FTOCAPO", "RECAPO", "AGGCAPO"));
			for (String benchmark : benchmarks) {
				BenchmarkInfo bench = new BenchmarkInfo(tool, benchmark, trials);
				for (int trial = 1; trial <= trials; trial++) {
					for (int config = 0; config < configs.length; config++) {
						String static_check_time = "";
						boolean static_race_checked = false;
						boolean benchmark_started = false;
						boolean unordered_pairs = false;
						try {
							String file = System.getProperty("user.home")+"/exp-output/"+output_dir+"/"
									+(benchmark.equals("htwo") ? "h2" : benchmark)+"9"+(benchmark.equals("lusearch") ? "-fixed" : "")
									+"/adapt/gc_default/"+(configs[config].equals("base")?"base_rrharness":"rr_"+configs[config])
									+"/var/"+trial+"/output.txt";
							System.out.println("loading: " + file);
							input = new BufferedReader(new FileReader(file));
						} catch (FileNotFoundException e) {
							System.out.println("failed input");
							continue;
						}
						long raceTotalDynamic = 0;
						long raceTotalStatic = 1;
						while ((line = input.readLine()) != null) {
							if (line.contains("[main: ----- ----- -----     Benchmark Meep Meep: Iter")) {
								benchmark_started = true;
							}
							if (benchmark_started && line.contains("  <threadCount> ")) {
								bench.setMaxLiveThread_count(configs[config], line.split("threadCount> ")[1].split(" <")[0], trial, trials);
							}
							if (benchmark_started && line.contains("  <threadMaxActive> ")) {
								bench.setTotalThread_count(configs[config], line.split("threadMaxActive> ")[1].split(" <")[0], trial, trials);
							}
							if (benchmark_started && line.contains("[main: ----- ----- -----     Benchmark Thpthpthpth: ")) {
								bench.setConfig_total_time(configNames[config], line.split(": ")[2].split("\\.")[0], bench_time, trial, trials); //milliseconds
							}
							if (benchmark_started && line.contains("    <counter><name> \"" + tool + ":")) { //xml counts
								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial);// == trials);
							}
							if (line.contains("Maximum resident set size (kbytes): ")) {
								bench.setConfig_mem(configNames[config], line.split(": ")[1], trial, trials);
							}
							
							//Count races
							if (tool.equals("DC") && line.contains("[main: Unordered Pairs Race Counts]")) {
								unordered_pairs = true;
							}
							if (benchmark_started && !contains(line, raceIdentifier, tool, configNames, config, fieldRace).isEmpty()) {
								if (configNames[config].equals("FT")) {
									if (line.contains("errorTotal> ")) {
										raceTotalDynamic = Long.parseLong(line.split("errorTotal> ")[1].split(" <")[0]);
										bench.setRace_types(configs[config], "FTDynamic", Long.toString(raceTotalDynamic), tool, trial, trials);
									} else if (line.contains("distinctErrorTotal> ")) {
										raceTotalStatic = Long.parseLong(line.split("distinctErrorTotal> ")[1].split(" <")[0]);
										bench.setRace_types(configs[config], "FT", Long.toString(raceTotalStatic), tool, trial, trials);
									}
								} else {
									if (tool.equals("DC")) {
										if (unordered_pairs) {
											bench.setRace_types(configs[config], contains(line, raceIdentifier, tool, configNames, config, fieldRace)+"UP", line.split(" ")[0], tool, trial, trials);
										} else {
											bench.setRace_types(configs[config], contains(line, raceIdentifier, tool, configNames, config, fieldRace), line.split(" ")[0], tool, trial, trials);
										}
									} else if (tool.equals("PIP")) {
										if (fieldRace) { //fields
											raceTotalDynamic = Long.parseLong(line.split("count> ")[1].split(" <")[0]);
											String raceType = getRaceType(raceTypeTotal, configNames, config);
											bench.setRace_types(configs[config], raceType+"Dynamic", Long.toString(raceTotalDynamic), tool, trial, trials);
											bench.setRace_types(configs[config], raceType, Long.toString(raceTotalStatic), tool, trial, trials);
										} else /*!fieldRace*/ { //single second site
											if (line.contains(" statically unique race(s)")) { //static single second site
												raceTotalStatic = Long.parseLong(line.split(" statically unique race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												bench.setRace_types(configs[config], raceType, Long.toString(raceTotalStatic), tool, trial, trials);
											} else if (line.contains(" dynamic race(s)")) { //dynamic single second site
												raceTotalDynamic = Long.parseLong(line.split(" dynamic race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												bench.setRace_types(configs[config], raceType+"Dynamic", Long.toString(raceTotalDynamic), tool, trial, trials);
											}
										}
									}
								}
							}
								
							if (benchmark_started && (line.contains("Checking WDC-race") || line.contains("Checking CAPO-race") || line.contains("Checking PIP-race"))) {
								String race = "";
								if (line.contains("Checking WDC-race")) {
									race = line.split("Checking WDC-race \\(")[1].split("\\) for event pair")[0];
								} else if (line.contains("Checking CAPO-race")) {
									race = line.split("Checking CAPO-race \\(")[1].split("\\) for event pair")[0];
								} else if (line.contains("Checking PIP-race")) {
									race = line.split("Checking PIP-race \\(")[1].split("\\) for event pair")[0];
								}
								if (static_check_time.isEmpty()) {
									static_race_checked = true;
									bench.setLatest_race(race, (static_check_time.isEmpty() ? true : false));
									bench.getLatest_race().set_distance(Long.parseLong(line.split("distance: ")[1]));
									bench.getLatest_race().set_static_trial(trial);
									bench.setRaces();
								} else {
									if (bench.getRaces().containsKey(race) && bench.getRaces().get(race).get_static_trial() == trial) {
										bench.getRaces().get(race).set_distance(Long.parseLong(line.split("distance: ")[1]));
									}
								}
							}
							if (benchmark_started && (line.contains("[main: Static Race Check Time: ") || line.contains("[main: Static DC Race Check Time: "))) {
								static_check_time = (static_race_checked ? line.split(": ")[2].split("]")[0] : "0");
								bench.setStatic_check_time(configs[config], static_check_time, trial == trials);
							}
							if (benchmark_started && (line.contains("[main: Dynamic Race Check Time: ") || line.contains("[main: Dynamic DC Race Check Time: "))) {
								String dynamic_check_time = line.split(": ")[2].split("]")[0];
								bench.setDynamic_check_time(configs[config], dynamic_check_time, trial == trials);
							}
							if (benchmark_started && line.contains("Iteration = ") && static_check_time.isEmpty()) {
								bench.getLatest_race().addTotal_iterations(Long.parseLong(line.split("= ")[1]));
							}
							if (benchmark_started && (line.contains("Found acq->rel") || line.contains("Found rel->acq")) && static_check_time.isEmpty()) {
								bench.getLatest_race().addTotal_edges_added(1);
							}
							if (benchmark_started && line.contains("Assertion ")) {
								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " | trial: " + trial + " | line: " + line);
							}
							if (benchmark_started && line.contains("[main: BackReorder Set Getting Stuck:")) {
								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " | trial: " + trial + " has a stuck race.");
							}
							if (benchmark_started && line.contains("Cycle reaches first node : true")) {
								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
							}
							if (benchmark_started && line.contains("Cycle reaches second node : true")) {
								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
							}
						}
					}
				}
				benchmarks_info.add(bench);
			}
			input.close();
			outputBenchInfo(benchmarks_info, output, trials, configs, configNames, tool);
			output.close();
			System.out.println("Finished closing output file");
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static String getRaceType(LinkedList<String> raceTypeTotal, String[] configNames, int config) {
		for (String type : raceTypeTotal) {
			if (configNames[config].equals(type)) {
				return type;
			}
		}
		return "";
	}
	public static String contains(String test, LinkedList<String> trueSet, String tool, String[] configNames, int config, boolean fieldRace) {
		if (configNames[config].equals("FT") && (test.contains("  <errorTotal> ") || test.contains("  <distinctErrorTotal> ")) ) {
			return "FastTrack";
		} else if ((tool.equals("DC") && configNames[config].equals("PIP")) || tool.equals("PIP")) {
			for (int i = 0; i < trueSet.size(); i++) {
				if (tool.equals("DC")) {					
					if (test.contains(" statically unique " + trueSet.get(i) + "-race(s)")) {
						if (configNames[config].equals("PIP")) {
							return "PIP" + trueSet.get(i);
						} else {
							return trueSet.get(i);
						}
					}
					if (test.contains(" dynamic " + trueSet.get(i) + "-race(s)")) {
						if (configNames[config].equals("PIP")) {
							return "PIP" + trueSet.get(i) + "Dynamic";
						} else {
							return trueSet.get(i) + "Dynamic";
						}
					}
				} else if (tool.equals("PIP")) {
					if (!fieldRace) {
						if (test.contains(" statically unique race(s)") || test.contains(" dynamic race(s)")) { //single second site
							return "PIP";
						}
					} else if (fieldRace) {
						if (test.contains("      <error> <name> " + trueSet.get(i) + " </name> <count> ")) { //fields
							return trueSet.get(i);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < trueSet.size(); i++) {
				if (test.contains("      <error> <name> FastTrack </name> <count> ")) {
					return "FastTrack";
				} else if (configNames[config].equals("HB"+trueSet.get(i))) { //To handle HBwG, HBwRaceEdge, and HBnoGwRaceEdge configurations
					if (test.contains(" statically unique " + "HB" + "-race(s)")) {
						return "HB" + trueSet.get(i);
					}
					if (test.contains(" dynamic " + "HB" + "-race(s)")) {
						return "HB" + trueSet.get(i) + "Dynamic";
					}
					break;
				} else if (configNames[config].equals(trueSet.get(i)) || configNames[config].equals("HB"+trueSet.get(i)) || configNames[config].equals(trueSet.get(i)+"noG") || configNames[config].equals(trueSet.get(i)+"Exc") || configNames[config].equals(trueSet.get(i)+"noGExc") || configNames[config].equals(trueSet.get(i).substring(1)+"Exc")) {
					if (test.contains(" statically unique " + trueSet.get(i) + "-race(s)")) {
						return configNames[config];
					}
					if (test.contains(" dynamic " + trueSet.get(i) + "-race(s)")) {
						return configNames[config] + "Dynamic";
					}
					break;
				}
			}
		}
		return "";
	}
	public static boolean matches(String test, LinkedList<String> trueSet) {
		for (int i = 0; i < trueSet.size(); i++) {
			if (trueSet.get(i).equals(test)) {
				return true;
			}
		}
		return false;
	}
	public static boolean matches(String test, String [] trueSet) {
		for (int i = 0; i < trueSet.length; i++) {
			if (trueSet[i].equals(test)) {
				return true;
			}
		}
		return false;
	}
	
	public static void outputBenchInfo(LinkedList<BenchmarkInfo> benchmarks, BufferedWriter output, int trials, String [] configs, String [] configNames, String tool) throws IOException {
		HashMap<String, double[]> geoMeanTime = new HashMap<String, double[]>();
		for (String configName : configNames) {
			geoMeanTime.put(configName, new double[benchmarks.size()]);
		}
		HashMap<String, double[]> geoMeanMem = new HashMap<String, double[]>();
		for (String configName : configNames) {
			geoMeanMem.put(configName, new double[benchmarks.size()]);
		}
		HashMap<String, double[]> totalRace = new HashMap<String, double[]>();
		int benchIndex = 0;
		
		for (BenchmarkInfo bench : benchmarks) {
			String observed_config = (tool.equals("DC") ? "wdc" : "pip_fto_re_capo");
			output.write(bench.getCount_Total(tool, observed_config, "Events"));
			output.write(bench.getCount_Total(tool, observed_config, "NoFPEvents"));
			if (extraStats) {
				//Note: Counts are collected and recorded for only a single configuration. 
				//TODO: Verify that these counts should be equivalent across configurations.
				assert bench.getCounts().get(observed_config) != null;
				String[] countsPIP = {"base", "empty", "ft", "pip_hb", "pip_fto_hb", "pip_wcp", "pip_fto_wcp", "pip_fto_re_wcp", "pip_dc", "pip_fto_dc", "pip_fto_re_dc", "pip_capo", "pip_fto_capo", "pip_fto_re_capo", "pip_agg"};
				String[] countsNamePIP = {"Base", "Empty", "FT", "HB", "FTOHB", "WCP", "FTOWCP", "REWCP", "DC", "FTODC", "REDC", "CAPO", "FTOCAPO", "RECAPO", "AGGCAPO"};
				for (int i = 0; i < countsPIP.length; i++) {
					if (bench.getCounts().get(countsPIP[i]) != null) bench.getCounts().get(countsPIP[i]).recordCounts(output, countsNamePIP[i]);
				}
//				if (bench.getCounts().get(observed_config) != null) bench.getCounts().get(observed_config).recordCounts(output);
				//Note: The following count set is only collected for CAPO_Opt configuration
				if (bench.getCounts().get("pip_fto_re_capo") != null) {
					bench.getCounts().get("pip_fto_re_capo").getRuleACounts(output);
				}
				//Note: The following two count sets are only collected for CAPO configuration
				if (bench.getCounts().get("pip_capo") != null) {
					bench.getCounts().get("pip_capo").getCAPOSetCounts(output);
					bench.getCounts().get("pip_capo").getCAPOMapCounts(output);
				}
			}
			output.write(bench.getMaxLiveThread_count(observed_config));
			output.write(bench.getTotalThread_count(observed_config));
			for (String config_time : bench.getConfig_total_time(configNames, geoMeanTime, benchIndex)) {
				output.write(config_time);
			}
			output.write(bench.getStatic_check_time());
			output.write(bench.getDynamic_check_time());
			//TODO: move this to the top so it can be set appropriately
			String[] memsDC = {"Base", "HB", "FT", "WCP", "WDCnoGExc", "WDCnoG", "WDCExc", "WDC", "CAPOnoGExc", "CAPOnoG", "CAPOExc", "CAPO"};
			String[] memsPIP = {"Base", "FT", "HB", "FTOHB", "WCP", "FTOWCP", "REWCP", "DC", "FTODC", "REDC", "CAPO", "FTOCAPO", "RECAPO", "AGGCAPO"};
			String[] mems = tool.equals("DC") ? memsDC : memsPIP;
			for (String config_mem : bench.getConfig_mem(tool, mems, geoMeanMem, benchIndex)) {
				output.write(config_mem);
			}
			//TODO: move this to the top so it can be set appropriately
			String[] typesDC = {"HB", "HBDynamic", "FT", "FTDynamic", "WCP", "WCPDynamic", 
					"WDCnoGExc", "WDCnoGExcDynamic", "WDCnoG", "WDCnoGDynamic", "WDCExc", "WDCExcDynamic", "WDC", "WDCDynamic", 
					"CAPOnoGExc", "CAPOnoGExcDynamic", "CAPOnoG", "CAPOnoGDynamic", "CAPOExc", "CAPOExcDynamic", "CAPO", "CAPODynamic", "PIP", "PIPDynamic",
					"HBUP", "HBDynamicUP", "WCPUP", "WCPDynamicUP", "WDCnoGUP", "WDCnoGDynamicUP", "WDCUP", "WDCDynamicUP", "CAPOnoGUP", "CAPOnoGDynamicUP", "CAPOUP", "CAPODynamicUP", "PIPUP", "PIPDynamicUP",
					"PIPHB", "PIPHBDynamic", "PIPWCP", "PIPWCPDynamic", "PIPWDC", "PIPWDCDynamic", "PIPCAPO", "PIPCAPODynamic", "PIPPIP", "PIPPIPDynamic"};
			String[] typesHB = {"HB", "HBDynamic", "HBwG", "HBwGDynamic", "HBwRaceEdge", "HBwRaceEdgeDynamic", "HBnoGwRaceEdge", "HBnoGwRaceEdgeDynamic"};
			String[] typesPIP = {"FT", "FTDynamic", "HB", "HBDynamic", "FTOHB", "FTOHBDynamic", 
					"WCP", "WCPDynamic", "FTOWCP", "FTOWCPDynamic", "REWCP", "REWCPDynamic", 
					"DC", "DCDynamic", "FTODC", "FTODCDynamic", "REDC", "REDCDynamic",
					"CAPO", "CAPODynamic", "FTOCAPO", "FTOCAPODynamic", "RECAPO", "RECAPODynamic",
					"AGGCAPO", "AGGCAPODynamic"};
			String[] types = tool.equals("DC") ? typesDC : typesPIP;
			for (String race_type : bench.getRace_types(tool, types, totalRace, benchIndex, benchmarks.size())) {
				output.write(race_type);
			}
			benchIndex++;
		}
		calcGeoMean(geoMeanTime, "Time", configNames, output);
		calcGeoMean(geoMeanMem, "Mem", configNames, output);
		calcTotalRace(totalRace, "Total", output);
		output.newLine();
	}
	
	public static void calcGeoMean(HashMap<String, double[]> geoMean, String stat, String[] configNames, BufferedWriter output) throws IOException {
		String toolName = tool.equals("DC") ? "SLOW" : "FAST"; 
		for (String configName : configNames) {
			double[] benchStats = geoMean.get(configName);
			int failures = EventCounts.failedTrials(benchStats); //failed benchmarks
			if (failures > 0) benchStats = EventCounts.resize(benchStats, benchStats.length - failures);
			String geoVal = stat.equals("Time") ? "\\rna" : "\\memna";
			if (benchStats.length != 0) {
				geoVal = String.valueOf(BenchmarkInfo.getTwoSigsDouble(getGeo(benchStats)));
				if (stat.equals("Mem") && geoVal.length() > 4 && !geoVal.contains(".")) geoVal = BenchmarkInfo.getParenthesis(geoVal.substring(0, geoVal.length()-3));
				if (geoVal.length() > 3 && geoVal.substring(geoVal.length()-2).equals(".0")) {
					geoVal = geoVal.substring(0,geoVal.length()-2);
				}
			}
			output.write("\\newcommand{\\"+toolName+configName+stat+"GeoMean}{"+geoVal+"}\n");
		}
	}
	
	public static double getGeo(double[] array) {
		double total = 1;
		for (int i = 0; i < array.length; i++) {
			total *= array[i];
		}
		return Math.pow(total, (1.0/array.length));
	}
	
	public static void calcTotalRace(HashMap<String, double[]> totalRace, String stat, BufferedWriter output) throws IOException {
		String toolName = tool.equals("DC") ? "SLOW" : "FAST";
		for (String type : totalRace.keySet()) {
			double[] total_race = totalRace.get(type);
			int failures = EventCounts.failedTrials(total_race); //failed benchmarks
			if (failures > 0) total_race = EventCounts.resize(total_race, total_race.length - failures);
			String raceCount = "\\rna";
			if (total_race.length != 0) {
				raceCount = String.valueOf(BenchmarkInfo.round(EventCounts.getSum(total_race)));
				if (raceCount.length() > 4 && !raceCount.contains(".")) raceCount = BenchmarkInfo.getParenthesis(raceCount);
			}
			output.write("\\newcommand{\\"+toolName+type+stat+"}{"+raceCount+"}\n");
		}
	}
}
