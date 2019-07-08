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
	static final String[] configsST = {"base", "ft", "pip_fto_hb", "pip_fto_wcp", "pip_fto_re_wcp", "pip_fto_dc", "pip_fto_re_dc", "pip_fto_capo", "pip_fto_re_capo"};
	static final String[] configNamesST = {"Base", "FT", "FTOHB", "FTOWCP", "REWCP", "FTODC", "REDC", "FTOCAPO", "RECAPO"};
	static final String[] configsUN = {"base", "ft", "hb", "hbwcp", "dc_noG_exc", "dc_exc", "capo_noG_exc", "capo_exc"};
	static final String[] configNamesUN = {"Base", "FT", "HB", "WCP", "DCnoGExc", "DCExc", "CAPOnoGExc", "CAPOExc"};
	static final boolean fieldRace = false; //true = field, false = single second site [results represent single second site]
	static String tool;
	static int trials;
	static boolean extraStats;
	static String output_dir;
	
	public static void main (String [] args) {
		tool = args[0].toUpperCase(); //Unopt or ST
		trials = Integer.parseInt(args[1]); //# of Trials
		extraStats = Boolean.getBoolean(args[2]); //false for DC tool and false if counts are not collected.
		if (tool.equals("Unopt")) extraStats = false;
		output_dir = args[3];
		
		// (Slow tool) Unoptimized analyses or (Fast tool) FTO and ST analyses 
		String[] configs = tool.equals("Unopt") ? configsUN : configsST;
		String[] configNames = tool.equals("Unopt") ? configNamesUN : configNamesST;
		
		LinkedList<BenchmarkInfo> benchmarks_info = new LinkedList<BenchmarkInfo>();
		BufferedReader input = null;
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/git/PIP-paper/result-macros/" + output_dir + ".tex"));
			String line;
			String bench_time = "";
			System.out.println("Parsing result files...");
			LinkedList<String> raceIdentifierUnopt = new LinkedList<String>(Arrays.asList("HB", "wG", "wRaceEdge", "noGwRaceEdge", "WCP", "DC", "CAPO", "CAPOOPT", "CAPOOPTALT", "CAPORE", "CAPOREALT", "CAPOREOPT"));
			LinkedList<String> raceIdentifierSmartTrack = new LinkedList<String>(Arrays.asList("SmartTrack", "FastTrack"));
			LinkedList<String> raceIdentifier = (tool.equals("Unopt") ? raceIdentifierUnopt : raceIdentifierSmartTrack);
			LinkedList<String> raceTypeTotal = new LinkedList<String>(Arrays.asList("FTOHB", "FTOWCP", "REWCP", "FTODC", "REDC", "FTOCAPO", "RECAPO"));
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
							if (benchmark_started && line.contains("ShadowThread: threadTotal")) {
								bench.setTotalThread_count(configs[config], line.split("name>                  <value> ")[1].split(" <")[0], trial, trials);
							}
							if (benchmark_started && line.contains("ShadowThread: threadMax")) {
								bench.setMaxLiveThread_count(configs[config], line.split("name>                    <value> ")[1].split(" <")[0], trial, trials);
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
							if (tool.equals("Unopt") && line.contains("[main: Unordered Pairs Race Counts]")) {
								unordered_pairs = true; //turned off for field names counting races
							}
							if (benchmark_started && !contains(line, raceIdentifier, tool, configNames, config, fieldRace).isEmpty()) {
								if (configNames[config].equals("FT")) {
									if (line.contains("errorTotal> ")) {
										raceTotalDynamic = Long.parseLong(line.split("errorTotal> ")[1].split(" <")[0]);
										bench.setRace_types("FTDynamic", Long.toString(raceTotalDynamic), trial, trials);
									} else if (line.contains("distinctErrorTotal> ")) {
										raceTotalStatic = Long.parseLong(line.split("distinctErrorTotal> ")[1].split(" <")[0]);
										bench.setRace_types("FT", Long.toString(raceTotalStatic), trial, trials);
									}
								} else {
									if (tool.equals("Unopt")) {
										if (unordered_pairs) {
											bench.setRace_types(contains(line, raceIdentifier, tool, configNames, config, fieldRace)+"UP", line.split(" ")[0], trial, trials);
										} else {
											bench.setRace_types(contains(line, raceIdentifier, tool, configNames, config, fieldRace), line.split(" ")[0], trial, trials);
										}
									} else if (tool.equals("PIP")) {
										if (fieldRace) { //fields
											raceTotalDynamic = Long.parseLong(line.split("count> ")[1].split(" <")[0]);
											String raceType = getRaceType(raceTypeTotal, configNames, config);
											bench.setRace_types(raceType+"Dynamic", Long.toString(raceTotalDynamic), trial, trials);
											bench.setRace_types(raceType, Long.toString(raceTotalStatic), trial, trials);
										} else /*!fieldRace*/ { //single second site
											if (line.contains(" statically unique race(s)")) { //static single second site
												raceTotalStatic = Long.parseLong(line.split(" statically unique race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												bench.setRace_types(raceType, Long.toString(raceTotalStatic), trial, trials);
											} else if (line.contains(" dynamic race(s)")) { //dynamic single second site
												raceTotalDynamic = Long.parseLong(line.split(" dynamic race\\(s\\)")[0]);
												String raceType = getRaceType(raceTypeTotal, configNames, config);
												bench.setRace_types(raceType+"Dynamic", Long.toString(raceTotalDynamic), trial, trials);
											}
										}
									}
								}
							}
								
							if (benchmark_started && (line.contains("Checking DC-race") || line.contains("Checking CAPO-race") || line.contains("Checking PIP-race"))) {
								String race = "";
								if (line.contains("Checking DC-race")) {
									race = line.split("Checking DC-race \\(")[1].split("\\) for event pair")[0];
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
		} else if (tool.equals("SmartTrack")) {
			for (int i = 0; i < trueSet.size(); i++) {
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
		} else {
			for (int i = 0; i < trueSet.size(); i++) {
				if (test.contains("      <error> <name> FastTrack </name> <count> ")) {
					return "FastTrack";
				} else if (configNames[config].equals(trueSet.get(i)) || configNames[config].equals(trueSet.get(i)+"Exc") || configNames[config].equals(trueSet.get(i)+"noGExc") || configNames[config].equals(trueSet.get(i).substring(1)+"Exc")) {
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
	
	public static void outputBenchInfo(LinkedList<BenchmarkInfo> benchmarks, BufferedWriter output, int trials, String[] configs, String[] configNames, String tool) throws IOException {
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
			String observed_config = (tool.equals("Unopt") ? "dc_exc" : "pip_fto_re_capo");
			output.write(bench.getCount_Total(tool, observed_config, "Events"));
			output.write(bench.getCount_Total(tool, observed_config, "NoFPEvents"));
			if (extraStats) {
				//Note: Counts are collected and recorded for only a single configuration. 
				assert bench.getCounts().get(observed_config) != null;
				String[] countsPIP = {"base", "ft", "pip_fto_hb", "pip_fto_wcp", "pip_fto_re_wcp", "pip_fto_dc", "pip_fto_re_dc", "pip_fto_capo", "pip_fto_re_capo"};
				String[] countsNamePIP = {"Base", "FT", "FTOHB", "FTOWCP", "REWCP", "FTODC", "REDC", "FTOCAPO", "RECAPO"};
				for (int i = 0; i < countsPIP.length; i++) {
					if (bench.getCounts().get(countsPIP[i]) != null) bench.getCounts().get(countsPIP[i]).recordCounts(output, countsNamePIP[i]);
				}
				//Note: The following count set is only collected for CAPO_Opt configuration
				if (bench.getCounts().get("pip_fto_re_capo") != null) {
					bench.getCounts().get("pip_fto_re_capo").getRuleACounts(output);
				}
			}
			output.write(bench.getMaxLiveThread_count(observed_config));
			output.write(bench.getTotalThread_count(observed_config));
			for (String config_time : bench.getConfig_total_time(configNames, geoMeanTime, benchIndex)) {
				output.write(config_time);
			}
			output.write(bench.getStatic_check_time());
			output.write(bench.getDynamic_check_time());
			String[] memsUnopt = {"Base", "HB", "FT", "WCP", "DCnoGExc", "DCExc", "CAPOnoGExc", "CAPOExc"};
			String[] memsSmartTrack = {"Base", "FT", "FTOHB", "FTOWCP", "REWCP", "FTODC", "REDC", "FTOCAPO", "RECAPO"};
			String[] mems = tool.equals("Unopt") ? memsUnopt : memsSmartTrack;
			for (String config_mem : bench.getConfig_mem(tool, mems, geoMeanMem, benchIndex)) {
				output.write(config_mem);
			}
			String[] typesUnopt = {"FT", "FTDynamic", "HB", "HBDynamic", "WCP", "WCPDynamic", 
					"DCnoGExc", "DCnoGExcDynamic", "DCExc", "DCExcDynamic", 
					"CAPOnoGExc", "CAPOnoGExcDynamic", "CAPOExc", "CAPOExcDynamic"};
			String[] typesSmartTrack = {"FT", "FTDynamic", "FTOHB", "FTOHBDynamic", 
					"FTOWCP", "FTOWCPDynamic", "REWCP", "REWCPDynamic", 
					"FTODC", "FTODCDynamic", "REDC", "REDCDynamic",
					"FTOCAPO", "FTOCAPODynamic", "RECAPO", "RECAPODynamic"};
			String[] types = tool.equals("Unopt") ? typesUnopt : typesSmartTrack;
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
		String toolName = tool.equals("Unopt") ? "SLOW" : "FAST"; 
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
		String toolName = tool.equals("Unopt") ? "SLOW" : "FAST";
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
