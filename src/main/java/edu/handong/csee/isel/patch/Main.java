package edu.handong.csee.isel.patch;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.handong.csee.isel.githubcommitparser.GithubPatchCollector;

/**
 * -i, URL or URI(github.com, reference file having github URLs, Local
 * Repository) -o, directory of result file. [-r], reference relative to bug
 * commit. [-m], minimum printing of lines. [-x], maximum printing of lines.
 * 
 * If is there '-r', check that commit message have the pattern by reference to
 * '-r' option value. Else, check that commit message have the 'bug' or 'fix'
 * keyword.
 * 
 * @author imseongbin
 */
public class Main {
	String gitRepositoryPath = null;
	String githubURL = null;
	String listOfGithubURLFile = null;
	String resultDirectory = null;
	String reference = null;
	int conditionMax = 0;
	int conditionMin = 0;
	boolean help;

	public static void main(String[] args) {
		Main bc = new Main();
		bc.run(args);
	}

	public void run(String[] args) {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}

			/* start main */

			if (gitRepositoryPath != null) {
// 2. when user do not put reference
				LocalGitRepositoryPatchCollector gr = new LocalGitRepositoryPatchCollector(gitRepositoryPath,
						resultDirectory, reference, conditionMax, conditionMin);
				gr.run();

			} else if (githubURL != null || listOfGithubURLFile != null) {
// 1. add min, max Option
// 2. '.java'
// 3. reference
				GithubPatchCollector gh = new GithubPatchCollector(githubURL, resultDirectory, listOfGithubURLFile,
						String.valueOf(conditionMin));
				gh.run();
			}

			/* end main */

		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			String input = cmd.getOptionValue("i");

			try {
				if (input.contains("github.com")) {
					githubURL = input;
				} else {
					File file = new File(input);
					if (file.exists()) {
						if (file.isDirectory()) {
							gitRepositoryPath = input;
						} else {
							listOfGithubURLFile = input;
						}
					} else {
						throw new Exception("input file not exist!");
					}
				}

				if (cmd.hasOption("x") || cmd.hasOption("m")) {
					if (cmd.hasOption("x") && cmd.hasOption("m")) {
						conditionMax = Integer.parseInt(cmd.getOptionValue("x"));
						conditionMin = Integer.parseInt(cmd.getOptionValue("m"));
						if (conditionMin > conditionMax) {
							throw new Exception("Max must be bigger than min!");
						}

					} else {
						throw new Exception("'x' and 'm' Option must be together!");
					}

				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				printHelp(options);
				return false;
			}

			reference = cmd.getOptionValue("r");
			resultDirectory = cmd.getOptionValue("o");
			help = cmd.hasOption("h");

		} catch (Exception e) {
			printHelp(options);
			return false;
		}

		return true;
	}

	private Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("i").longOpt("input").desc(
				"Three input type: URL or URI(github.com, reference file having github URLs, Local " + "Repository)")
				.hasArg().argName("URI or URL").required().build());

		options.addOption(Option.builder("o").longOpt("result").desc("directory will have result file").hasArg()
				.argName("directory").required().build());

		options.addOption(Option.builder("r").longOpt("reference")
				.desc("If you have list of bug commit IDs, make a file to have the list, and push the file").hasArg()
				.argName("reference relative to bug").build());

		options.addOption(Option.builder("x").longOpt("Maxline")
				.desc("Set a Max lines of each result patch. Only count '+' and '-' lines.").hasArg()
				.argName("Max lines of patch").build());

		options.addOption(Option.builder("m").longOpt("Minline")
				.desc("Set a Min lines of each result patch. This Option need to be used with 'M' Option(MaxLine).")
				.hasArg().argName("Min lines of patch").build());

		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());

		return options;
	}

	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "Collecting bug-patch program";
		String footer = "\nPlease report issues at https://github.com/HGUISEL/BugPatchCollector/issues";
		formatter.printHelp("BugPatchCollector", header, options, footer, true);
	}

}
