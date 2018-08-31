import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class parseDC {
	static final String[] benchmarks = {"avrora", "batik", "htwo", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "xalan"};
	static final int trials = 5; //Integer.parseInt(args[1]);
	static final String tool = "PIP"; //DC or PIP
	static final boolean fieldRace = false; //true = field, false = single second site
	static final boolean extraStats = true; //false if counts are not collected.
	static final String output_dir = /*args[0];*/ "PIP_fastTool_shortestRaceEdge";
	
	public static void main (String [] args) {
		//Vindicator tool //configs -> wdc_testconfig | configNames = DCLite
//		String [] configs = {"base", "empty", "hbwcp", "wdc_noG", "wdc"};//, "capo_only", "pip_only"};//, "wdc_noG", "capo_noG", "pip_noG"};
//		String [] configNames = {"Base", "Empty", "HBWCP", "DCLite", "WDC"};//, "CAPO", "PIP"};// ,"DCLite", "CAPOLite", "PIPLite"};
		//PIP tool (fast tool)
		String [] configs = {"base", "empty", "ft", "pip_hb", "pip_wcp", "pip_dc", "pip_capo", "pip_capoOpt", "pip_capoOptAlt"}; //, "pip_pip"};
		String [] configNames = {"Base", "Empty", "FT", "HB", "WCP", "DC", "CAPO", "CAPOOPT", "CAPOOPTALT"}; //, "PIP"};
		//Quiet PIP tool (fast tool)
//		String [] configs = {"base", "empty", "ft", "pipQ_hb", "pipQ_wcp", "pipQ_dc", "pipQ_capo", "pipQ_capoOpt", "pipQ_capoOptAlt"};
//		String [] configNames = {"Base", "Empty", "FT", "HB", "WCP", "DC", "CAPO", "CAPOOPT", "CAPOOPTALT"};
		//PIP tool (slow tool)
//		String [] configs = {"base", "empty", "ft", "hb", "hbwcp", "wdc_noG", "wdc"};//, "wdc_noG", "wdc_exc", "capo"};//, "wdc_exc", "capo_exc", "pip_exc", "pip"};
//		String [] configNames = {"Base", "Empty", "FT", "HB", "HBWCP", "WDCLite", "WDC"};//, "WDCLite", "WDCExc", "CAPOFull"};//, "DCExc", "CAPOExc", "PIPExc", "PIP"};
		LinkedList<BenchmarkInfo> benchmarks_info = new LinkedList<BenchmarkInfo>();
		BufferedReader input = null;
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter("result-macros.txt"));
			String line;
			String memory = "";
			String bench_time = "";
			System.out.println("Parsing result files...");
			LinkedList<String> HBSet = new LinkedList<String>(Arrays.asList("HB"));
			if (tool.equals("DC")) HBSet.addAll(Arrays.asList("HB", "HBWCP", "PIP"));
			LinkedList<String> WCPSet = new LinkedList<String>(Arrays.asList("WCP"));
			if (tool.equals("DC")) WCPSet.addAll(Arrays.asList("HBWCP", "PIP"));
			LinkedList<String> DCSet = new LinkedList<String>(Arrays.asList("DC"));
			if (tool.equals("DC")) DCSet.addAll(Arrays.asList("DCExc", "PIP"));
			LinkedList<String> CAPOSet = new LinkedList<String>(Arrays.asList("CAPO"));
			if (tool.equals("DC")) CAPOSet.addAll(Arrays.asList("CAPOExc", "PIP"));
			LinkedList<String> PIPSet = new LinkedList<String>(Arrays.asList("PIP"));
			if (tool.equals("DC")) PIPSet.addAll(Arrays.asList("PIPExc", "PIP"));
			LinkedList<String> raceIdentifier = (tool.equals("DC") ? new LinkedList<String>(Arrays.asList("HB", "WCP", "WDC", "CAPO", /*"PIP"*/ "CAPOOPT", "CAPOOPTALT")) : new LinkedList<String>(Arrays.asList("PIP", "FastTrack")));
			LinkedList<String> raceTypeTotal = new LinkedList<String>(Arrays.asList("HB", "WCP", "DC", "CAPO", /*"PIP"*/ "CAPOOPT", "CAPOOPTALT"));
			for (String benchmark : benchmarks) {
				BenchmarkInfo bench = new BenchmarkInfo(benchmark, trials);
				for (int trial = 1; trial <= trials; trial++) {
					for (int config = 0; config < configs.length; config++) {
						String static_check_time = "";
						boolean static_race_checked = false;
						boolean benchmark_started = false;
						try {
							String file = System.getProperty("user.home")+"/exp-output/"+output_dir+"/"
									+(benchmark.equals("htwo") ? "h2" : benchmark)+"9"+(benchmark.equals("lusearch") ? "-fixed" : "")+"/adapt/gc_default/"+(configs[config].equals("base")?"base_rrharness":"rr_"+configs[config])+"/var/"+trial+"/output.txt";
							System.out.println("attempted input: " + file);
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
							if (benchmark_started && line.contains("    <counter><name> \"" + tool + ": Total Events")) { //slow tool (DC)
								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial);// == trials);
//							} else if (benchmark_started && line.contains("    <counter><name> \"" + tool + ": Total Ops")) { //fast tool (PIP)
//								//Need to subtract total fast path taken from total ops 
//								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial == trials);
//							} else if (benchmark_started && line.contains("    <counter><name> \"" + tool + ": Total Fast Path Taken")) { //fast tool (PIP)
//								//Need to subtract total fast path taken from total ops
//								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial == trials);
							} else if (benchmark_started && line.contains("[main: event total: ")) {
								bench.setCounts(configs[config], "Total Events", Long.parseLong(line.split(": ")[2].split("]")[0]), trial);// == trials);
							}
							if (benchmark_started && line.contains("  <threadCount> ")) {
								bench.setMaxLiveThread_count(configs[config], line.split("threadCount> ")[1].split(" <")[0], trial, trials);
							}
							if (benchmark_started && line.contains("  <threadMaxActive> ")) {
								bench.setTotalThread_count(configs[config], line.split("threadMaxActive> ")[1].split(" <")[0], trial, trials);
							}
							if (benchmark_started && line.contains("[main: ----- ----- -----     Benchmark Thpthpthpth: ")) {
								bench.setConfig_total_time(configNames[config], configs[config], line.split(": ")[2].split("\\.")[0], bench_time, trial == trials); //milliseconds
								//Full GC should not be output once total RR time is output
								if (memory.isEmpty()) {
									memory = "0"; //No Full GC during benchmark execution
								}
								bench.setConfig_mem(configNames[config], memory, trial == trials);
								if (benchmark.equals("xalan") && config == 3) {
									System.out.println(line.split(": ")[2].split("\\.")[0]);
								}
							}
							if (benchmark_started && line.contains("[Full GC ")) {
								if (line.contains("K(")) {
									memory = line.split("K->")[1].split("K\\(")[0];
								}
							}
							if (benchmark_started && line.contains("    <counter><name> \"" + tool + ":")) { //xml counts
								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial);// == trials);
							}
							
							//Count races
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
										bench.setRace_types(configs[config], contains(line, raceIdentifier, tool, configNames, config, fieldRace), line.split(" ")[0], tool, trial, trials);
									} else if (tool.equals("PIP")) {
										if (fieldRace) { //fields
											raceTotalDynamic = Long.parseLong(line.split("count> ")[1].split(" <")[0]);
											String raceType = getRaceType(raceTypeTotal, configNames, config);
											if (raceType.equals("DC")) raceType = "WDC";
											bench.setRace_types(configs[config], raceType+"Dynamic", Long.toString(raceTotalDynamic), tool, trial, trials);
											bench.setRace_types(configs[config], raceType, Long.toString(raceTotalStatic), tool, trial, trials);
										} else /*!fieldRace*/ { //single second site
											if (line.contains(" statically unique race(s)")) { //static single second site
												raceTotalStatic = Long.parseLong(line.split(" statically unique race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												if (raceType.equals("DC")) raceType = "WDC";
												bench.setRace_types(configs[config], raceType, Long.toString(raceTotalStatic), tool, trial, trials);
											} else if (line.contains(" dynamic race(s)")) { //dynamic single second site
												raceTotalDynamic = Long.parseLong(line.split(" dynamic race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												if (raceType.equals("DC")) raceType = "WDC";
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
//							if (benchmark_started && line.contains("[main: BackReorder Set Getting Stuck:")) {
//								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " has a stuck race.");
//							}
//							if (benchmark_started && line.contains("Cycle reaches first node : true")) {
//								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
//							}
//							if (benchmark_started && line.contains("Cycle reaches second node : true")) {
//								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
//							}
//							if (benchmark_started && line.contains("Checking PIP-race")) {
//								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has PIP-race.");
//							}
//							if (benchmark_started && line.contains("Checking CAPO-race")) {
//								System.out.println("config: " + configs[config] + " | benchmark: " + bench.benchmark + " trial: " + trial + " has PIP-race.");
//							}
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
				} else if (configNames[config].equals(trueSet.get(i)) || configNames[config].equals("HB"+trueSet.get(i)) || configNames[config].equals(trueSet.get(i)+"Exc") || configNames[config].equals(trueSet.get(i).substring(1)+"Exc")) {
					if (test.contains(" statically unique " + trueSet.get(i) + "-race(s)")) {
						return trueSet.get(i);
					}
					if (test.contains(" dynamic " + trueSet.get(i) + "-race(s)")) {
						return trueSet.get(i) + "Dynamic";
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
		String race_summary = "";
		long race_index = 0;
		long[] race_totals = {0/*ft_total*/, 0/*ft_dynamic_total*/, 0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/, 0/*capo_total*/, 0/*capo_dynamic_total*/, 0/*pip_total*/, 0/*pip_dynamic_total*/};
		long[] full_pip_race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/, 0/*capo_total*/, 0/*capo_dynamic_total*/, 0/*pip_total*/, 0/*pip_dynamic_total*/};
		long[] capo_race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/};
		long[] pip_race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/};
		//Runtime/Memory Overhead Table
		for (BenchmarkInfo bench : benchmarks) {
			String observed_config = "pip_capoOpt";
			output.write(bench.getCount_Total(tool, "Events"));
			output.write(bench.getCount_Total(tool, "NoFPEvents"));
			if (extraStats) {
				//Note: Counts are collected and recorded for only a single configuration. 
				//TODO: Verify that these counts should be equivalent across configurations.
				assert bench.getCounts().get(observed_config) != null;
				bench.getCounts().get(observed_config).recordCounts(output);
				//Note: The following count set is only collected for CAPO_Opt configuration
				if (bench.getCounts().get("pip_capoOpt") != null) {
					bench.getCounts().get("pip_capoOpt").getRuleACounts(output);
				}
				//Note: The following two count sets are only collected for CAPO configuration
				if (bench.getCounts().get("pip_capo") != null) {
					bench.getCounts().get("pip_capo").getCAPOSetCounts(output);
					bench.getCounts().get("pip_capo").getCAPOMapCounts(output);
				}
			}
			output.write(bench.getMaxLiveThread_count(observed_config));
			output.write(bench.getTotalThread_count(observed_config));			
			for (String config_time : bench.getConfig_total_time(configs, configNames)) {
				output.write(config_time);
			}
			output.write(bench.getStatic_check_time());
			String temp = bench.getStatic_check_time().split("\\{")[2].split("\\}")[0];
			output.write(bench.getDynamic_check_time());
			temp = bench.getDynamic_check_time().split("\\{")[2].split("\\}")[0];
			for (String config_mem : bench.getConfig_mem()) {
				output.write(config_mem);
			}
			for (String race_type : bench.getRace_types(tool)) {
				output.write(race_type);
			}
			for (String capo_race_type : bench.getCapoRace_types()) {
				output.write(capo_race_type);
			}
			for (String pip_race_type : bench.getPIPRace_types()) {
				output.write(pip_race_type);
			}
		}
		// DC Races: Static/Dynamic Race Table
		if (false) {//true) {//tool.equals("PIP")) {
		System.out.println("Static DC races (dynamic races)");
		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
			Long [] raceFormat = new Long[12];
			raceFormat[10] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("FT")));
			raceFormat[11] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("FTDynamic")));
			raceFormat[0] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("HB")));
			raceFormat[1] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("HBDynamic")));
			raceFormat[2] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("WCP")));
			raceFormat[3] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("WCPDynamic")));
			raceFormat[4] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("WDC")));
			raceFormat[5] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("WDCDynamic")));
			raceFormat[6] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("CAPO")));
			raceFormat[7] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("CAPODynamic")));
			raceFormat[8] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("PIP")));
			raceFormat[9] = BenchmarkInfo.round(EventCounts.getAvg(bench.types.get("PIPDynamic")));
			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
			for (String type_key : bench.types.keySet()) {
				if (type_key.equals("FT")) {
					race_totals[10] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("HB")) {
					race_totals[0] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("WCP")) {
					race_totals[2] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("WDC")) {
					race_totals[4] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("CAPO")) {
					race_totals[6] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIP")) {
					race_totals[8] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("FTDynamic")) { 
					race_totals[11] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("HBDynamic")) {	
					race_totals[1] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("WCPDynamic")) {
					race_totals[3] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("WDCDynamic")) {
					race_totals[5] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("CAPODynamic")) {
					race_totals[7] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPDynamic")) {
					race_totals[9] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPHB")) {
					full_pip_race_totals[0] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key))); 
				} else if (type_key.equals("PIPWCP")) {
					full_pip_race_totals[2] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPWDC")) {
					full_pip_race_totals[4] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPCAPO")) {
					full_pip_race_totals[6] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPPIP")) {
					full_pip_race_totals[8] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPHBDynamic")) {
					full_pip_race_totals[1] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPWCPDynamic")) {
					full_pip_race_totals[3] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPWDCDynamic")) {
					full_pip_race_totals[5] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPCAPODynamic")) {
					full_pip_race_totals[7] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				} else if (type_key.equals("PIPPIPDynamic")) {
					full_pip_race_totals[9] += BenchmarkInfo.round(EventCounts.getAvg(bench.types.get(type_key)));
				}
			}
		}
		String [] raceFormatTotal = new String[12];
		raceFormatTotal[10] = ""+race_totals[10];
		raceFormatTotal[11] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[11]));
		raceFormatTotal[0] = ""+race_totals[0];
		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[1]));
		raceFormatTotal[2] = ""+race_totals[2];
		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[3]));
		raceFormatTotal[4] = ""+race_totals[4];
		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[5]));
		raceFormatTotal[6] = ""+race_totals[6];
		raceFormatTotal[7] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[7]));
		raceFormatTotal[8] = ""+race_totals[8];
		raceFormatTotal[9] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[9]));
		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
		output.write("\\newcommand{\\FTTotal}{" + race_totals[10] + "}\n");
		output.write("\\newcommand{\\FTDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[11])) + "}\n");
		output.write("\\newcommand{\\HBTotal}{" + race_totals[0] + "}\n");
		output.write("\\newcommand{\\HBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[1])) + "}\n");
		output.write("\\newcommand{\\WCPTotal}{" + race_totals[2] + "}\n");
		output.write("\\newcommand{\\WCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[3])) + "}\n");
		output.write("\\newcommand{\\WDCTotal}{" + race_totals[4] + "}\n");
		output.write("\\newcommand{\\WDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[5])) + "}\n");
		output.write("\\newcommand{\\CAPOTotal}{" + race_totals[6] + "}\n");
		output.write("\\newcommand{\\CAPODynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[7])) + "}\n");
		output.write("\\newcommand{\\PIPTotal}{" + race_totals[8] + "}\n");
		output.write("\\newcommand{\\PIPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[9])) + "}\n");
		
		output.write("\\newcommand{\\PIPHBTotal}{" + full_pip_race_totals[0] + "}\n");
		output.write("\\newcommand{\\PIPHBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(full_pip_race_totals[1])) + "}\n");
		output.write("\\newcommand{\\PIPWCPTotal}{" + full_pip_race_totals[2] + "}\n");
		output.write("\\newcommand{\\PIPWCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(full_pip_race_totals[3])) + "}\n");
		output.write("\\newcommand{\\PIPWDCTotal}{" + full_pip_race_totals[4] + "}\n");
		output.write("\\newcommand{\\PIPWDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(full_pip_race_totals[5])) + "}\n");
		output.write("\\newcommand{\\PIPCAPOTotal}{" + full_pip_race_totals[6] + "}\n");
		output.write("\\newcommand{\\PIPCAPODynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(full_pip_race_totals[7])) + "}\n");
		output.write("\\newcommand{\\PIPPIPTotal}{" + full_pip_race_totals[8] + "}\n");
		output.write("\\newcommand{\\PIPPIPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(full_pip_race_totals[9])) + "}\n");
		if (false) { //tool.equals("DC")) {
		// CAPO Races: Static/Dynamic Race Table
		System.out.println("Static CAPO races (dynamic races)");
		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
			Long [] raceFormat = new Long[6];
			raceFormat[0] = BenchmarkInfo.round(bench.capo_types.get("HB")/trials);
			raceFormat[1] = BenchmarkInfo.round(bench.capo_types.get("HBDynamic")/trials);
			raceFormat[2] = BenchmarkInfo.round(bench.capo_types.get("WCP")/trials);
			raceFormat[3] = BenchmarkInfo.round(bench.capo_types.get("WCPDynamic")/trials);
			raceFormat[4] = BenchmarkInfo.round(bench.capo_types.get("WDC")/trials);
			raceFormat[5] = BenchmarkInfo.round(bench.capo_types.get("WDCDynamic")/trials);
			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
			for (String type_key : bench.capo_types.keySet()) {
				if (type_key.equals("HB")) {
					capo_race_totals[0] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				} else if (type_key.equals("WCP")) {
					capo_race_totals[2] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				} else if (type_key.equals("WDC")) {
					capo_race_totals[4] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				} else if (type_key.equals("HBDynamic")) {	
					capo_race_totals[1] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				} else if (type_key.equals("WCPDynamic")) {
					capo_race_totals[3] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				} else if (type_key.equals("WDCDynamic")) {
					capo_race_totals[5] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
				}
			}
		}
		raceFormatTotal = new String[6];
		raceFormatTotal[0] = ""+capo_race_totals[0];
		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[1]));
		raceFormatTotal[2] = ""+capo_race_totals[2];
		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[3]));
		raceFormatTotal[4] = ""+capo_race_totals[4];
		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[5]));
		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
		output.write("\\newcommand{\\CAPOHBTotal}{" + capo_race_totals[0] + "}\n");
		output.write("\\newcommand{\\CAPOHBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[1])) + "}\n");
		output.write("\\newcommand{\\CAPOWCPTotal}{" + capo_race_totals[2] + "}\n");
		output.write("\\newcommand{\\CAPOWCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[3])) + "}\n");
		output.write("\\newcommand{\\CAPOWDCTotal}{" + capo_race_totals[4] + "}\n");
		output.write("\\newcommand{\\CAPOWDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[5])) + "}\n");
		// PIP Races: Static/Dynamic Race Table
		System.out.println("Static PIP races (dynamic races)");
		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
			Long [] raceFormat = new Long[6];
			if (!bench.pip_types.isEmpty()) {
			raceFormat[0] = BenchmarkInfo.round(bench.pip_types.get("HB")/trials);
			raceFormat[1] = BenchmarkInfo.round(bench.pip_types.get("HBDynamic")/trials);
			raceFormat[2] = BenchmarkInfo.round(bench.pip_types.get("WCP")/trials);
			raceFormat[3] = BenchmarkInfo.round(bench.pip_types.get("WCPDynamic")/trials);
			raceFormat[4] = BenchmarkInfo.round(bench.pip_types.get("WDC")/trials);
			raceFormat[5] = BenchmarkInfo.round(bench.pip_types.get("WDCDynamic")/trials);
			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
			for (String type_key : bench.pip_types.keySet()) {
				if (type_key.equals("HB")) {
					pip_race_totals[0] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				} else if (type_key.equals("WCP")) {
					pip_race_totals[2] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				} else if (type_key.equals("WDC")) {
					pip_race_totals[4] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				} else if (type_key.equals("HBDynamic")) {	
					pip_race_totals[1] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				} else if (type_key.equals("WCPDynamic")) {
					pip_race_totals[3] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				} else if (type_key.equals("WDCDynamic")) {
					pip_race_totals[5] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
				}
			}
			}
		}
		raceFormatTotal = new String[6];
		raceFormatTotal[0] = ""+pip_race_totals[0];
		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[1]));
		raceFormatTotal[2] = ""+pip_race_totals[2];
		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[3]));
		raceFormatTotal[4] = ""+pip_race_totals[4];
		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[5]));
		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
		output.write("\\newcommand{\\PIPHBTotal}{" + pip_race_totals[0] + "}\n");
		output.write("\\newcommand{\\PIPHBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[1])) + "}\n");
		output.write("\\newcommand{\\PIPWCPTotal}{" + pip_race_totals[2] + "}\n");
		output.write("\\newcommand{\\PIPWCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[3])) + "}\n");
		output.write("\\newcommand{\\PIPWDCTotal}{" + pip_race_totals[4] + "}\n");
		output.write("\\newcommand{\\PIPWDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[5])) + "}\n");
		//Static DC-only Race Table
		output.write("\\newcommand{\\racenum}[2]{#2}");
		System.out.println("Static DC-only race");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
			boolean race_added = false;
			System.out.println(bench_name + " Races:");
			for (String race : bench.getRaces().keySet()) {
				if (!race_summary.isEmpty()) {
					race_summary += "\\cline{2-5}";
				}
				race_summary += "\\racenum{"+(race_index++)+"}{"+(bench_name.equals("htwo") ? "h2" : bench_name) + "&" + bench.getRaces().get(race).toString()+"}\\\\ \n";
				System.out.println("  " + bench.getRaces().get(race).toString());
				bench_name = "";
				race_added = true;
			}
			if (race_added) {
				output.write("\\newcommand{\\racesum"+bench.getBenchmark()+"}{"+race_summary+"}");
				race_summary = "";
			}
		}
		}
		}
		output.newLine();
	}
}
