import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class parseDC {
	public static void main (String [] args) {
//		String output_dir = "Vindicator_10trial_wCounts";
//		String output_dir = "parallel_Vindicator_10trials"; //args[0];
//		String output_dir = "PIP_5trials";
		String output_dir = "parallel_Vindicator_10trials_jython";
		String [] benchmarks = {"jython"};//{"avrora", "batik", "htwo", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "xalan"};
		String [] configs = {"base", "empty", "hbwcp", "wdc_testconfig", "wdc"};//, "capo", "pip"};
		String [] configNames = {"Base", "Empty", "HBWCP", "DCLite", "DC"};//, "CAPO", "PIP"};
		int trials = 10; //Integer.parseInt(args[1]);
		LinkedList<BenchmarkInfo> benchmarks_info = new LinkedList<BenchmarkInfo>();
		BufferedReader input = null;
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter("result-macros.txt"));
			String line;
			String memory = "";
			String bench_time = "";
			System.out.println("Parsing result files...");
			for (String benchmark : benchmarks) {
				BenchmarkInfo bench = new BenchmarkInfo(benchmark, trials);
				for (int trial = 1; trial <= trials; trial++) {
					for (int config = 0; config < configs.length; config++) {
						String static_check_time = "";
						boolean static_race_checked = false;
						boolean benchmark_started = false;
						try {
//							input = new BufferedReader(new FileReader(System.getProperty("user.home")+"/exp-output/"+output_dir+"/"
//									+(benchmark.equals("htwo") ? "h2" : benchmark)+"9"+(benchmark.equals("lusearch") ? "-fixed" : "")+"/adapt/gc_default/"+"rr_"+configs[config]+"/var/"+trial+"/output.txt"));
							input = new BufferedReader(new FileReader(System.getProperty("user.home")+"/exp-output/"+output_dir+"/"
									+(benchmark.equals("htwo") ? "h2" : benchmark)+"9"+(benchmark.equals("lusearch") ? "-fixed" : "")+"/adapt/gc_default/"+(configs[config].equals("base")?"base_rrharness":"rr_"+configs[config])+"/var/"+trial+"/output.txt"));
						} catch (FileNotFoundException e) {
							continue;
						}
						while ((line = input.readLine()) != null) {
							if (line.contains("[main: ----- ----- -----     Benchmark Meep Meep: Iter")) {
								benchmark_started = true;
							}
							if (benchmark_started && line.contains("    <counter><name> \"DC: ")) {
								bench.setCounts(configs[config], line.split(": ")[1].split("\"")[0], Long.parseLong(line.split("> ")[3].split(" </")[0].trim().replaceAll(",","")), trial == trials);
							} else if (benchmark_started && line.contains("[main: event total: ")) {
								bench.setCounts(configs[config], "Total Events", Long.parseLong(line.split(": ")[2].split("]")[0]), trial == trials);
							}
							if (benchmark_started && line.contains("<threadCount>")) {
								bench.setMaxLiveThread_count(configs[config], line.split("> ")[1].split(" <")[0], trial == trials);
							}
							if (benchmark_started && line.contains("<threadMaxActive>")) {
								bench.setTotalThread_count(configs[config], line.split("> ")[1].split(" <")[0], trial == trials);
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
//							} else if (line.matches(".*([0-9]+):([0-9.0-9]+)elapsed.*")) {
//								long minuteTOmilliseconds = Long.parseLong(line.split(" ")[2].split("elapsed")[0].split(":")[0]) * 60 * 1000;
//								long milliseconds = (long) (Double.parseDouble(line.split(" ")[2].split("elapsed")[0].split(":")[1]) * 1000);
//								if (benchmark.equals("htwo") && config == 0) {
//									System.out.println(minuteTOmilliseconds + " " + milliseconds);
//								}
//								bench.setConfig_total_time(configNames[config], configs[config], ""+(minuteTOmilliseconds + milliseconds), bench_time, trial == trials); //milliseconds
//							}
							if (benchmark_started && line.contains("[Full GC ")) {
								if (line.contains("K(")) {
									memory = line.split("K->")[1].split("K\\(")[0];
								}
							}
							if (benchmark_started && line.contains(" statically unique HB-race(s)")) {
								bench.setRace_types(configs[config], "HB", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains(" statically unique WCP-race(s)")) {
								bench.setRace_types(configs[config], "WCP", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains(" statically unique WDC-race(s)")) {
								bench.setRace_types(configs[config], "WDC", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains(" dynamic HB-race(s)")) {
								bench.setRace_types(configs[config], "HBDynamic", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains(" dynamic WCP-race(s)")) {
								bench.setRace_types(configs[config], "WCPDynamic", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains(" dynamic WDC-race(s)")) {
								bench.setRace_types(configs[config], "WDCDynamic", line.split(" ")[0], trial == trials);
							}
							if (benchmark_started && line.contains("Checking WDC-race")) {
//								String race = line.split("\\(")[1].split("\\)")[0];
								String race = line.split("Checking WDC-race \\(")[1].split("\\) for event pair")[0];
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
							if (benchmark_started && line.contains("[main: BackReorder Set Getting Stuck:")) {
								System.out.println("config: " + configs[config] + "benchmark: " + bench.benchmark + " has a stuck race.");
							}
							if (benchmark_started && line.contains("Cycle reaches first node : true")) {
								System.out.println("config: " + configs[config] + "benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
							}
							if (benchmark_started && line.contains("Cycle reaches second node : true")) {
								System.out.println("config: " + configs[config] + "benchmark: " + bench.benchmark + " trial: " + trial + " has reachable cycle.");
							}
						}
					}
				}
				benchmarks_info.add(bench);
			}
			input.close();
			outputBenchInfo(benchmarks_info, output, trials, configs, configNames);
			output.close();
			System.out.println("Finished closing output file");
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void outputBenchInfo(LinkedList<BenchmarkInfo> benchmarks, BufferedWriter output, int trials, String [] configs, String [] configNames) throws IOException {
		String race_summary = "";
		long race_index = 0;
		String[] race_types = {"HB","WCP","WDC","HBDynamic","WCPDynamic","WDCDynamic"};
		long[] race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/};
		long[] capo_race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/};
		long[] pip_race_totals = {0/*hb_total*/, 0/*hb_dynamic_total*/, 0/*wcp_total*/, 0/*wcp_dynamic_total*/, 0/*dc_total*/, 0/*dc_dynamic_total*/};
		//Runtime/Memory Overhead Table
//		System.out.printf("%-9s| %-6s | %-7s || %4s %6s %7s | %23s || %6s %8s %9s | %10s\n","Program","Events","#Thr","Base","HB+WCP", "DC w/oG", "Vindicator", "Base", "HB+WCP", "DC w/oG", "Vindicator");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
//			String [] tableFormat = new String[14];
//			int tableIndex = 0;
//			tableFormat[tableIndex++] = bench_name;
			output.write(bench.getCount_Total("wdc", "Events"));
//			tableFormat[tableIndex++] = bench.getCount_Total("wdc", "Events").split("\\{")[2].split("\\}")[0]+"M";
			output.write(bench.getCount_Total("wdc", "NoFPEvents"));
//			for (String config : bench.getCounts().keySet()) {
//				bench.getCounts().get(config).printCounts();
//			}
			output.write(bench.getMaxLiveThread_count());
//			tableFormat[tableIndex++] = bench.getMaxLiveThread_count().split("\\{")[2].split("\\}")[0];
			output.write(bench.getTotalThread_count());
//			tableFormat[tableIndex++] = bench.getTotalThread_count().split("\\{")[2].split("\\}")[0];			
			for (String config_time : bench.getConfig_total_time(configs, configNames)) {
				output.write(config_time);
//				tableFormat[tableIndex++] = config_time.split("\\{")[2].split("\\}")[0];
			}
			output.write(bench.getStatic_check_time());
			String temp = bench.getStatic_check_time().split("\\{")[2].split("\\}")[0];
//			tableFormat[tableIndex++] = (temp.equals("\\rzero")?"0":temp);
			output.write(bench.getDynamic_check_time());
			temp = bench.getDynamic_check_time().split("\\{")[2].split("\\}")[0];
//			tableFormat[tableIndex++] = (temp.equals("\\rzero")?"0":temp);
			for (String config_mem : bench.getConfig_mem()) {
				output.write(config_mem);
//				tableFormat[tableIndex++] = config_mem.split("\\{")[2].split("\\}")[0] + " ";
			}
//			System.out.printf("%-9s| %6s | %2s (%-2s) || %4s %6s %7s | %6s (%6s) + %5s || %6s %8s %9s | %10s\n", tableFormat[0],tableFormat[1],tableFormat[2],tableFormat[3],tableFormat[4],tableFormat[5],tableFormat[6],tableFormat[7],tableFormat[8],tableFormat[9],tableFormat[10],tableFormat[11],tableFormat[12],tableFormat[13]);
			for (String race_type : bench.getRace_types()) {
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
		System.out.println("Static DC races (dynamic races)");
		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
		for (BenchmarkInfo bench : benchmarks) {
			String bench_name = bench.getBenchmark();
			Long [] raceFormat = new Long[6];
			raceFormat[0] = BenchmarkInfo.round(bench.types.get("HB")/trials);
			raceFormat[1] = BenchmarkInfo.round(bench.types.get("HBDynamic")/trials);
			raceFormat[2] = BenchmarkInfo.round(bench.types.get("WCP")/trials);
			raceFormat[3] = BenchmarkInfo.round(bench.types.get("WCPDynamic")/trials);
			raceFormat[4] = BenchmarkInfo.round(bench.types.get("WDC")/trials);
			raceFormat[5] = BenchmarkInfo.round(bench.types.get("WDCDynamic")/trials);
			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
			for (String type_key : bench.types.keySet()) {
				if (type_key.equals("HB")) {
					race_totals[0] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				} else if (type_key.equals("WCP")) {
					race_totals[2] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				} else if (type_key.equals("WDC")) {
					race_totals[4] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				} else if (type_key.equals("HBDynamic")) {	
					race_totals[1] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				} else if (type_key.equals("WCPDynamic")) {
					race_totals[3] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				} else if (type_key.equals("WDCDynamic")) {
					race_totals[5] += BenchmarkInfo.round(bench.types.get(type_key)/trials);
				}
			}
		}
		String [] raceFormatTotal = new String[6];
		raceFormatTotal[0] = ""+race_totals[0];
		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[1]));
		raceFormatTotal[2] = ""+race_totals[2];
		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[3]));
		raceFormatTotal[4] = ""+race_totals[4];
		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(race_totals[5]));
		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
		output.write("\\newcommand{\\HBTotal}{" + race_totals[0] + "}\n");
		output.write("\\newcommand{\\HBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[1])) + "}\n");
		output.write("\\newcommand{\\WCPTotal}{" + race_totals[2] + "}\n");
		output.write("\\newcommand{\\WCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[3])) + "}\n");
		output.write("\\newcommand{\\WDCTotal}{" + race_totals[4] + "}\n");
		output.write("\\newcommand{\\WDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(race_totals[5])) + "}\n");
//		// CAPO Races: Static/Dynamic Race Table
//		System.out.println("Static CAPO races (dynamic races)");
//		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
//		for (BenchmarkInfo bench : benchmarks) {
//			String bench_name = bench.getBenchmark();
//			Long [] raceFormat = new Long[6];
//			raceFormat[0] = BenchmarkInfo.round(bench.capo_types.get("HB")/trials);
//			raceFormat[1] = BenchmarkInfo.round(bench.capo_types.get("HBDynamic")/trials);
//			raceFormat[2] = BenchmarkInfo.round(bench.capo_types.get("WCP")/trials);
//			raceFormat[3] = BenchmarkInfo.round(bench.capo_types.get("WCPDynamic")/trials);
//			raceFormat[4] = BenchmarkInfo.round(bench.capo_types.get("WDC")/trials);
//			raceFormat[5] = BenchmarkInfo.round(bench.capo_types.get("WDCDynamic")/trials);
//			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
//			for (String type_key : bench.capo_types.keySet()) {
//				if (type_key.equals("HB")) {
//					capo_race_totals[0] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				} else if (type_key.equals("WCP")) {
//					capo_race_totals[2] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				} else if (type_key.equals("WDC")) {
//					capo_race_totals[4] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				} else if (type_key.equals("HBDynamic")) {	
//					capo_race_totals[1] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				} else if (type_key.equals("WCPDynamic")) {
//					capo_race_totals[3] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				} else if (type_key.equals("WDCDynamic")) {
//					capo_race_totals[5] += BenchmarkInfo.round(bench.capo_types.get(type_key)/trials);
//				}
//			}
//		}
//		raceFormatTotal = new String[6];
//		raceFormatTotal[0] = ""+capo_race_totals[0];
//		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[1]));
//		raceFormatTotal[2] = ""+capo_race_totals[2];
//		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[3]));
//		raceFormatTotal[4] = ""+capo_race_totals[4];
//		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[5]));
//		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
//		output.write("\\newcommand{\\CAPOHBTotal}{" + capo_race_totals[0] + "}\n");
//		output.write("\\newcommand{\\CAPOHBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[1])) + "}\n");
//		output.write("\\newcommand{\\CAPOWCPTotal}{" + capo_race_totals[2] + "}\n");
//		output.write("\\newcommand{\\CAPOWCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[3])) + "}\n");
//		output.write("\\newcommand{\\CAPOWDCTotal}{" + capo_race_totals[4] + "}\n");
//		output.write("\\newcommand{\\CAPOWDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(capo_race_totals[5])) + "}\n");
//		// PIP Races: Static/Dynamic Race Table
//		System.out.println("Static PIP races (dynamic races)");
//		System.out.printf("%-9s| %-13s | %-15s | %-11s\n", "Program", "HB-races", "WCP-races", "DC-races");
//		for (BenchmarkInfo bench : benchmarks) {
//			String bench_name = bench.getBenchmark();
//			Long [] raceFormat = new Long[6];
//			if (!bench.pip_types.isEmpty()) {
//			raceFormat[0] = BenchmarkInfo.round(bench.pip_types.get("HB")/trials);
//			raceFormat[1] = BenchmarkInfo.round(bench.pip_types.get("HBDynamic")/trials);
//			raceFormat[2] = BenchmarkInfo.round(bench.pip_types.get("WCP")/trials);
//			raceFormat[3] = BenchmarkInfo.round(bench.pip_types.get("WCPDynamic")/trials);
//			raceFormat[4] = BenchmarkInfo.round(bench.pip_types.get("WDC")/trials);
//			raceFormat[5] = BenchmarkInfo.round(bench.pip_types.get("WDCDynamic")/trials);
//			System.out.printf("%-9s| %-5s (%-5s) | %-6s (%-6s) | %-4s (%-6s)\n", bench_name, raceFormat[0], raceFormat[1], raceFormat[2], raceFormat[3], raceFormat[4], raceFormat[5]);
//			for (String type_key : bench.pip_types.keySet()) {
//				if (type_key.equals("HB")) {
//					pip_race_totals[0] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				} else if (type_key.equals("WCP")) {
//					pip_race_totals[2] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				} else if (type_key.equals("WDC")) {
//					pip_race_totals[4] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				} else if (type_key.equals("HBDynamic")) {	
//					pip_race_totals[1] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				} else if (type_key.equals("WCPDynamic")) {
//					pip_race_totals[3] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				} else if (type_key.equals("WDCDynamic")) {
//					pip_race_totals[5] += BenchmarkInfo.round(bench.pip_types.get(type_key)/trials);
//				}
//			}
//			}
//		}
//		raceFormatTotal = new String[6];
//		raceFormatTotal[0] = ""+pip_race_totals[0];
//		raceFormatTotal[1] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[1]));
//		raceFormatTotal[2] = ""+pip_race_totals[2];
//		raceFormatTotal[3] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[3]));
//		raceFormatTotal[4] = ""+pip_race_totals[4];
//		raceFormatTotal[5] = BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[5]));
//		System.out.printf("%-9s| %-5s (%-4s) | %-6s (%-5s) | %-4s (%-6s)\n", "Total", raceFormatTotal[0], raceFormatTotal[1], raceFormatTotal[2], raceFormatTotal[3], raceFormatTotal[4], raceFormatTotal[5]);
//		output.write("\\newcommand{\\PIPHBTotal}{" + pip_race_totals[0] + "}\n");
//		output.write("\\newcommand{\\PIPHBDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[1])) + "}\n");
//		output.write("\\newcommand{\\PIPWCPTotal}{" + pip_race_totals[2] + "}\n");
//		output.write("\\newcommand{\\PIPWCPDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[3])) + "}\n");
//		output.write("\\newcommand{\\PIPWDCTotal}{" + pip_race_totals[4] + "}\n");
//		output.write("\\newcommand{\\PIPWDCDynamicTotal}{" + BenchmarkInfo.getParenthesis(String.valueOf(pip_race_totals[5])) + "}\n");
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
		output.newLine();
	}
}
