package DiscordBot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import DiscordBot.GeneratedMission.MISSION_TYPE;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class SWNBOT extends ListenerAdapter {

	private static List<String> chatWords;
	private static List<String> startWords;
	private static List<String> lastWords;
	private static Map<String,String> madness_short;
	private static Map<String,String> madness_long;
	private static Map<String,String> madness_indefinite;
	
	private enum MADNESS_DURATION{
		SHORT,LONG,INDEFINITE
	};
	
	private static List<MessageChannel> channels;

	private enum FALLOUT_LEVEL {
		MINOR, MAJOUR, CRITICAL
	}

	private enum FALLOUT_TYPE {
		BLOOD, MIND, ECHO, FORTUNE, SUPPLIES
	}

	private static JSONObject gameData;
	private static JSONObject RPGData;
	public static BufferedImage btn1;
	public static BufferedImage btn2;
	public static BufferedImage btn3;
	public static BufferedImage btnA;
	public static BufferedImage btnB;
	public static BufferedImage exclamation;
	public static BufferedImage home;
	public static BufferedImage ico_import;
	public static BufferedImage power;
	public static BufferedImage question;
	public static BufferedImage singleplayer;
	public static BufferedImage star;
	public static BufferedImage target;
	public static BufferedImage warning;

	public static List<String> portraitArmourFiles;
	public static List<String> portraitHairFiles;
	public static List<String> portraitHeadFiles;
	public static List<String> portraitMiscFiles;

	public static BufferedImage[] portraitArmorImages;
	public static BufferedImage[] portraitHairImages;
	public static BufferedImage[] portraitHeadImages;
	public static BufferedImage[] portraitMiscImages;

	public static int[] DEFENCE_VALUES = { 0, 1, 1, 1, 2, 2, 2, 2, 3, 3 };

	public static HashMap<User, ISCharacter> characterMap;

	public static void main(String[] args) throws LoginException {
		loadData();
		chatWords = new ArrayList<String>();
		startWords = new ArrayList<String>();
		lastWords = new ArrayList<String>();
		loadMessageHistory();
		channels = new ArrayList<MessageChannel>();
		characterMap = new HashMap<User, ISCharacter>();

		JDABuilder builder = new JDABuilder(AccountType.BOT);
		InputStream inputFile = ClassLoader.getSystemClassLoader().getResourceAsStream("discordKey");
		Scanner sc = new Scanner(inputFile);
		String token = sc.nextLine();
		builder.setToken(token);
		sc.close();
		builder.addEventListener(new SWNBOT());
		builder.buildAsync();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			if (event.getAuthor().getName().equalsIgnoreCase("MEE6")) {
				if (event.getMessage().getContentRaw().contains("GG")) {
					event.getChannel().sendMessage("GG yourself in the ass, MEE6.").queue();
				} else {
					event.getChannel().sendMessage("Shut the fuck up MEE6! No one likes you").queue();
				}
			}
			return;
		}

		if (!channels.contains(event.getChannel())) {
			channels.add(event.getChannel());
			loadHistoryFromNewChannel(event.getChannel());
		}

		if (event.getMessage().getContentRaw().contentEquals("!ping")) {
			event.getChannel().sendMessage("Pong!").queue();

		} else {
			parseMessage(event);
		}
	}

	private void loadHistoryFromNewChannel(MessageChannel channel) {
		System.out.println("Reading history of new channel");
		MessageHistory mh = channel.getHistory();
		List<Message> messages = mh.retrievePast(100).complete();
		System.out.println("Found " + messages.size() + " messages in history");
		for (Message msg : messages) {
			if (!msg.getAuthor().isBot()) {
				recordNewWords(msg.getContentRaw());
			}
		}
	}

	private static void loadData() {
		InputStream inputFile = ClassLoader.getSystemClassLoader().getResourceAsStream("GameData.json");
		InputStream RPGfile = ClassLoader.getSystemClassLoader().getResourceAsStream("RPGCharacters.json");

		String text = null;
		try (Scanner scanner = new Scanner(inputFile)) {
			text = scanner.useDelimiter("\\A").next();
			scanner.close();
		}
		gameData = new JSONObject(text);

		text = null;
		try (Scanner scanner = new Scanner(RPGfile)) {
			text = scanner.useDelimiter("\\A").next();
			scanner.close();
		}
		RPGData = new JSONObject(text);

		// Load the Armour images
		InputStream portaitFiles = ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Armour");
		InputStreamReader isr = new InputStreamReader(portaitFiles, StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(isr);
		portraitArmourFiles = new ArrayList<String>();
		br.lines().forEach(portraitArmourFiles::add);

		portaitFiles = ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Hair");
		isr = new InputStreamReader(portaitFiles, StandardCharsets.UTF_8);
		br = new BufferedReader(isr);
		portraitHairFiles = new ArrayList<String>();
		br.lines().forEach(portraitHairFiles::add);

		portaitFiles = ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/misc");
		isr = new InputStreamReader(portaitFiles, StandardCharsets.UTF_8);
		br = new BufferedReader(isr);
		portraitMiscFiles = new ArrayList<String>();
		br.lines().forEach(portraitMiscFiles::add);

		portaitFiles = ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Head");
		isr = new InputStreamReader(portaitFiles, StandardCharsets.UTF_8);
		br = new BufferedReader(isr);
		portraitHeadFiles = new ArrayList<String>();
		br.lines().forEach(portraitHeadFiles::add);

		try {
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		portraitArmorImages = new BufferedImage[portraitArmourFiles.size()];
		portraitHairImages = new BufferedImage[portraitHairFiles.size()];
		portraitHeadImages = new BufferedImage[portraitHeadFiles.size()];
		portraitMiscImages = new BufferedImage[portraitMiscFiles.size()];

		for (int i = 0; i < portraitArmourFiles.size(); i++) {
			try {
				portraitArmorImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader()
						.getResourceAsStream("portrait/Armour/" + portraitArmourFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < portraitHairFiles.size(); i++) {
			try {
				portraitHairImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader()
						.getResourceAsStream("portrait/Hair/" + portraitHairFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < portraitHeadFiles.size(); i++) {
			try {
				portraitHeadImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader()
						.getResourceAsStream("portrait/Head/" + portraitHeadFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < portraitMiscFiles.size(); i++) {
			try {
				portraitMiscImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader()
						.getResourceAsStream("portrait/misc/" + portraitMiscFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		madness_short = new HashMap<String,String>();
		madness_long = new HashMap<String,String>();
		madness_indefinite = new HashMap<String,String>();
	}

	private void parseMessage(MessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		try {
			if (args[0].charAt(0) != '!') {
				if (!event.getAuthor().isBot()) {
					recordNewWords(event.getMessage().getContentRaw());
				}
				return;
			}
		} catch (Exception ex) {
			System.out.println("Unable to parse message '" + args[0] + "'");
		}
		
		switch (args[0]) {
		case "!help":
			String availableCommands = "";
			availableCommands += "!creature\n";
			availableCommands += "!person\n";
			availableCommands += "!problem\n";
			availableCommands += "!world\n";
			availableCommands += "!name\n";
			availableCommands += "!ttsname\n";
			availableCommands += "!map\n";
			availableCommands += "!speak\n";
			availableCommands += "!save\n";
			availableCommands += "!tts\n";
			availableCommands += "!portrait\n";
			event.getChannel().sendMessage(availableCommands).queue();
			break;
		case "!creature":
			event.getChannel().sendMessage(generateCreature()).queue();
			break;
		case "!person":
			event.getChannel().sendMessage(generatePerson()).queue();
			break;
		case "!problem":
			event.getChannel().sendMessage(generateProblem()).queue();
			break;
		case "!world":
			event.getChannel().sendMessage(generateWorld()).queue();
			break;
		case "!settlement":
			event.getChannel().sendMessage(settlementName()).queue();
			break;
		case "!name":
			try {
				if (args.length < 2) {
					event.getChannel().sendMessage(generateName(Integer.valueOf(args[1]))).queue();
				} else {
					String name = "";
					for (int i = 1; i < args.length; i++) {
						name += generateName(Integer.valueOf(args[i]));
						if (Integer.valueOf(args[i]) == 0) {
							name += ".";
						}
						name += " ";
					}
					event.getChannel().sendMessage(name).queue();
				}
			} catch (Exception ex) {
				System.out.println("unable to parse arg: " + args[1]);
				ex.printStackTrace();
				event.getChannel().sendMessage(
						"Error parsing command. Use !name (number of syllables) [optional](number of syllables)")
						.queue();
			}
			break;
		case "!ttsname":
			try {
				if (args.length < 2) {
					event.getChannel().sendMessage(generateName(Integer.valueOf(args[1]))).queue();
				} else {
					String name = "";
					for (int i = 1; i < args.length; i++) {
						name += generateName(Integer.valueOf(args[i]));
						if (Integer.valueOf(args[i]) == 0) {
							name += ".";
						}
						name += " ";
					}
					MessageBuilder mb = new MessageBuilder();
					mb.setTTS(true);
					mb.setContent(name);
					event.getChannel().sendMessage(mb.build()).queue();
				}
			} catch (Exception ex) {
				System.out.println("unable to parse arg: " + args[1]);
				ex.printStackTrace();
				event.getChannel().sendMessage(
						"Error parsing command. Use !name (number of syllables) [optional](number of syllables)")
						.queue();
			}
			break;
		case "!map":
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(genMap(), "jpg", baos);
				baos.flush();
				byte[] imageByteArray = baos.toByteArray();
				baos.close();
				event.getChannel().sendFile(imageByteArray, "Map.jpg").queue();
			} catch (Exception e) {
				event.getChannel().sendMessage("Unable to generate map." + e.getMessage()).queue();
			}
			break;
		case "!overwatch":
			event.getChannel().sendMessage(overwatch()).queue();
			break;
		case "!character":
			generateNewCharacter(event);
			break;
		case "!portrait":
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(genPortrait(), "jpg", baos);
				baos.flush();
				byte[] imageByteArray = baos.toByteArray();
				baos.close();
				event.getChannel().sendFile(imageByteArray, "Portrait.jpg").queue();
			} catch (Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate portrait. " + e.getMessage()).queue();
			}
			break;
		case "!speak":
		case "!tourettes":
			try {
				event.getChannel().sendMessage(whatsUp()).queue();
			} catch (Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		case "!hey":
			try {
				event.getChannel().sendMessage(ColinShit()).queue();
			} catch (Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		case "!tts":
			try {
				MessageBuilder mb = new MessageBuilder();
				mb.setTTS(true);
				mb.setContent((String) whatsUp());
				event.getChannel().sendMessage(mb.build()).queue();
			} catch (Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		case "!save":
			try {
				saveMessageHistory();
				event.getChannel().sendMessage("Message history saved.").queue();
			} catch (Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		case "!iron":
			parseIronsworn(event);
			break;
		case "!roll":
			try {
				if (args.length < 2) {
					event.getChannel().sendMessage(roll(0)).queue();
				} else {
					event.getChannel().sendMessage(roll(Integer.valueOf(args[1]))).queue();
				}
			} catch (Exception ex) {
				System.out.println("unable to parse arg: " + args[1]);
				ex.printStackTrace();
				event.getChannel()
						.sendMessage("Error parsing command. Use !roll (action roll bonus). \n For example: !roll 2")
						.queue();
			}
			break;
		case "!dnd":
			try {
				String raceList = "Human,Elf,Half-Elf,Gnome,Halfling,Orc,Half-Orc,Dragonborn,Dwarf,Teifling";
				String classList = "Barbarian,Bard,Cleric,Druid,Fighter,Monk,Paladin,Ranger,Rogue,Sorcerer,Warlock,Wizard";
				CharSequence name = generateName(ThreadLocalRandom.current().nextInt(3) + 1);
				CharSequence message = name + ", " + getRandomValue(raceList) + " " + getRandomValue(classList);
				event.getChannel().sendMessage(message).queue();
			} catch (Exception ex) {
				event.getChannel().sendMessage("Unable to generate character. " + ex.getMessage()).queue();
			}
			break;
		case "!fallout":
			try {
				CharSequence fallout = getFallout(FALLOUT_LEVEL.valueOf(args[1].toUpperCase()), FALLOUT_TYPE.valueOf(args[2].toUpperCase()));
				event.getChannel().sendMessage("```" + fallout + "```").queue();
			} catch (Exception ex) {
				event.getChannel()
						.sendMessage("Error parsing command. Use !fallout LEVEL TYPE.\n Possible LEVEL options: Critical, Majour, Minor\n Possible TYPE options: Blood, Mind, Echo, Fortune, Supplies").queue();
			}
			break;
		case "!resource":
			try {
				event.getChannel().sendMessage(getResource()).queue();
			} catch (Exception ex) {
				event.getChannel().sendMessage("Unable to generate resource.\n" + ex.getMessage()).queue();
			}
			break;
		case "!users":
			List<Member> users = event.getGuild().getMembers();
			List<String> members = new ArrayList<String>();
			for (Member m : users) {
				members.add("ID:" + m.getUser().getId() + " name:" + m.getUser().getName());
			}
			event.getChannel().sendMessage(members.toString()).queue();
			break;
		case "!madness":
			try {
				if(args.length == 1) {
					//Only used for testing
					event.getAuthor().openPrivateChannel().queue((channel) ->{
						channel.sendMessage(getMadness(MADNESS_DURATION.SHORT)).queue();
					});
				}else {
					switch (args[1]) {
					case"add":
						int id = -1;
						switch(args[2].toUpperCase()) {
						case "S":
							id = addMadness(event.getMessage().getContentRaw().substring(15), MADNESS_DURATION.SHORT);
							break;
						case "SHORT":
							id = addMadness(event.getMessage().getContentRaw().substring(19), MADNESS_DURATION.SHORT);
							break;
						case "L":
							id = addMadness(event.getMessage().getContentRaw().substring(15), MADNESS_DURATION.LONG);
							break;
						case "LONG":
							id = addMadness(event.getMessage().getContentRaw().substring(19), MADNESS_DURATION.LONG);
							break;
						case "I":
							id = addMadness(event.getMessage().getContentRaw().substring(15), MADNESS_DURATION.INDEFINITE);
							break;
						case "INDEFINITE":
							id = addMadness(event.getMessage().getContentRaw().substring(19), MADNESS_DURATION.INDEFINITE);
							break;
						default:
							event.getChannel().sendMessage("Invalid Command. Use !madness add [S|SHORT|L|LONG|I|INDEFINITE] [value] ").queue();
							break;
						}
						event.getChannel().sendMessage("Added new madness with ID: " + id).queue();
						saveMessageHistory();
						break;
					case "remove":
						boolean result = false;
						switch(args[2].toUpperCase()) {
						case "S":
						case "SHORT":
							result = removeMadness(MADNESS_DURATION.SHORT, args[3].toUpperCase());						
							break;
						case "L":
						case "LONG":
							result = removeMadness(MADNESS_DURATION.SHORT, args[3].toUpperCase());
							break;
						case "I":
						case "INDEFINITE":
							result = removeMadness(MADNESS_DURATION.SHORT, args[3].toUpperCase());
							break;
						default:
							event.getChannel().sendMessage("Invalid Command. Use !madness remove [S|SHORT|L|LONG|I|INDEFINITE] [ID] ").queue();
							break;
						}
						if(!result) {
							event.getChannel().sendMessage("Unable to remove madness with ID: " + args[2]).queue();
						} else {
							event.getChannel().sendMessage("Madness with ID: " + args[3] + " removed.").queue();
							saveMessageHistory();
						}
						break;
					case "list":
						event.getChannel().sendFile(new File("madness_short.txt")).queue();
						event.getChannel().sendFile(new File("madness_long.txt")).queue();
						event.getChannel().sendFile(new File("madness_indefinite.txt")).queue();
						break;
					default:
						try {
							switch (args[1].toUpperCase()) {
							case "S":
							case "SHORT":
								CharSequence mad = getMadness(MADNESS_DURATION.SHORT);
								event.getJDA().getUsersByName(args[2],true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("You've gone mad!\n" + mad ).queue();
								});	
								//send to colin also
								event.getJDA().getUsersByName("dirty_rez",true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("Madness assigned to " + args[2] + "\n" + mad).queue();
								});	
								break;
							case "L":
							case "LONG":
								CharSequence madl = getMadness(MADNESS_DURATION.LONG);
								event.getJDA().getUsersByName(args[2],true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("You've gone mad!\n" + madl ).queue();
								});	
								//send to colin also
								event.getJDA().getUsersByName("dirty_rez",true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("Madness assigned to " + args[2] + "\n" + madl).queue();
								});
								break;
							case "I":
							case "Indefinite":
								CharSequence madi = getMadness(MADNESS_DURATION.INDEFINITE);
								event.getJDA().getUsersByName(args[2],true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("You've gone mad!\n" + madi ).queue();
								});	
								//send to colin also
								event.getJDA().getUsersByName("dirty_rez",true).get(0).openPrivateChannel().queue((channel) -> {
									channel.sendMessage("Madness assigned to " + args[2] + "\n" + madi).queue();
								});
								break;
							default:
								event.getChannel().sendMessage("Invalid commans. Use !madness [s|short|l|long|i|indefinite] membername.\n\tFor example: !madness long squizm").queue();
								break;
							}
							
						} catch (Exception ex) {
							event.getChannel().sendMessage("Unable to find user: " + args[1] + ". Use !users to get a list of users names.").queue();
						}						
						break;
					}					
				}
			}catch (Exception ex) {
				event.getChannel().sendMessage("Unable to parse command.\n" + ex.getMessage()).queue();
			}
			break;	
			
		case "!tech":
			try {
				event.getChannel().sendMessage(getTechBabble()).queue();				
			}catch (Exception ex)
			{
				event.getChannel().sendMessage(ex.getMessage()).queue();
			}			
			break;
		case "!techtts":
			try {
				MessageBuilder mb = new MessageBuilder();
				mb.setTTS(true);
				mb.setContent((String) getTechBabble());
				event.getChannel().sendMessage(mb.build()).queue();
			}catch(Exception ex) {
				event.getChannel().sendMessage(ex.getMessage()).queue();
			}
		default:
			// event.getChannel().addReactionById(event.getMessageId(),
			// event.getGuild().getEmoteById(EMOTE_GROSS)).queue();
			break;
		}
	}

	private boolean removeMadness(MADNESS_DURATION s, String string) {
		switch(s) {
		case INDEFINITE:
			if(madness_indefinite.containsKey(string)) { madness_indefinite.remove(string); return true;}
			break;
		case LONG:
			if(madness_long.containsKey(string)) { madness_long.remove(string); return true;}
			break;
		case SHORT:
			if(madness_short.containsKey(string)) { madness_short.remove(string); return true;}	
			break;
		}					
		return false;
	}

	private int addMadness(String value, MADNESS_DURATION duration) {
		int id = -1;
		switch(duration) {
		case INDEFINITE:
			id = madness_indefinite.size();
			madness_indefinite.put(String.valueOf(id), value);
			break;
		case LONG:
			id = madness_long.size();
			madness_long.put(String.valueOf(id),value);
			break;
		case SHORT:
			id = madness_short.size();
			madness_short.put(String.valueOf(id), value);
			break;
		}		
		return id;
	}

	private CharSequence getMadness(MADNESS_DURATION duration) {
		String retval = "```";
		switch(duration) {
		case INDEFINITE:
			retval += madness_indefinite.get("" + ThreadLocalRandom.current().nextInt(madness_indefinite.size()));
			break;
		case LONG:
			retval += madness_long.get("" + ThreadLocalRandom.current().nextInt(madness_long.size()));
			break;
		case SHORT:
			retval += madness_short.get("" + ThreadLocalRandom.current().nextInt(madness_short.size()));
			break;	
		}
		retval += "```";
		return retval;
	}

	private void saveMessageHistory() {
		try {
			FileWriter writer = new FileWriter("output.txt");
			for (String word : chatWords) {
				writer.append(word + "\n");
			}
			writer.close();

			writer = new FileWriter("startWords.txt");
			for (String word : startWords) {
				writer.append(word + "\n");
			}
			writer.close();

			writer = new FileWriter("lastWords.txt");
			for (String word : lastWords) {
				writer.append(word + "\n");
			}
			writer.close();
			
			writer = new FileWriter("madness_short.txt");
			for (int i = 0; i < madness_short.size(); i++) {
				writer.append(madness_short.get(String.valueOf(i)) + "\n");
			}
			writer.close();
			
			writer = new FileWriter("madness_long.txt");
			for (int i = 0; i < madness_long.size(); i++) {
				writer.append(madness_long.get(String.valueOf(i)) + "\n");
			}
			writer.close();
			
			writer = new FileWriter("madness_indefinite.txt");
			for (int i = 0; i < madness_indefinite.size(); i++) {
				writer.append(madness_indefinite.get(String.valueOf(i)) + "\n");
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static CharSequence ColinShit() {
		String output = "";
		int numWords = ThreadLocalRandom.current().nextInt(4) + 3;
		for (int i = 0; i < numWords; i++) {
			output += chatWords.get(ThreadLocalRandom.current().nextInt(chatWords.size())) + " ";
		}
		output += chatWords.get(ThreadLocalRandom.current().nextInt(chatWords.size())) + ", ";

		List<String> prepositions = Arrays.asList("on", "in", "at", "to", "under", "over", "inside", "outside", "above",
				"below", "across", "through", "up", "down", "around", "past", "cruelty towards", "knowledge of",
				"trouble with", "age at", "attempt to", "inquiry into", "admitted to", "go to", "relate to", "Aboard",
				"About", "Above", "Absent", "Across", "After", "Against", "Along", "Alongside", "Amid", "Among",
				"Amongst", "Anti", "Around", "As", "At", "Before", "Behind", "Below", "Beneath", "Beside", "Besides",
				"Between", "Beyond", "But", "By", "Circa", "Concerning", "Considering", "Despite", "Down", "During",
				"Except", "Excepting", "Excluding", "Failing", "Following", "For", "From", "Given", "In", "Inside",
				"Into", "Like", "Minus", "Near", "Of", "Off", "On", "Onto", "Opposite", "Outside", "Over", "Past",
				"Per", "Plus", "Regarding", "Round", "Save", "Since", "Than", "Through", "To", "Toward", "Towards",
				"Under", "Underneath", "Unlike", "Until", "Up", "Upon", "Versus", "Via", "With", "Within", "Without",
				"Worth");
		output += prepositions.get(ThreadLocalRandom.current().nextInt(prepositions.size())).toLowerCase() + " ";
		output += lastWords.get(ThreadLocalRandom.current().nextInt(lastWords.size()));
		return output;
	}

	private static void loadMessageHistory() {
		try {
			Scanner scanner = new Scanner(new File("output.txt"));
			scanner.hasNext();
			while (scanner.hasNext()) {
				chatWords.add(scanner.next());
			}
			scanner.close();

			scanner = new Scanner(new File("startWords.txt"));
			scanner.hasNext();
			while (scanner.hasNext()) {
				startWords.add(scanner.next());
			}
			scanner.close();

			scanner = new Scanner(new File("lastWords.txt"));
			scanner.hasNext();
			while (scanner.hasNext()) {
				lastWords.add(scanner.next());
			}
			scanner.close();
			
			scanner = new Scanner(new File("madness_short.txt"));
			int count =0;
			while (scanner.hasNextLine()) {
				madness_short.put(String.valueOf(count), scanner.nextLine());
				count++;
			}
			scanner.close();
			
			scanner = new Scanner(new File("madness_long.txt"));
			count = 0;
			while (scanner.hasNextLine()) {
				madness_long.put(String.valueOf(count), scanner.nextLine());
				count++;
			}
			scanner.close();
			
			scanner = new Scanner(new File("madness_indefinite.txt"));
			count = 0;
			while (scanner.hasNextLine()) {
				madness_indefinite.put(String.valueOf(count), scanner.nextLine());
				count++;
			}
			scanner.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static CharSequence whatsUp() {
		String output = "";
		int numWords = ThreadLocalRandom.current().nextInt(4) + 3;
		output += startWords.get(ThreadLocalRandom.current().nextInt(startWords.size())) + " ";
		for (int i = 0; i < numWords; i++) {
			output += chatWords.get(ThreadLocalRandom.current().nextInt(chatWords.size())) + " ";
		}
		output += lastWords.get(ThreadLocalRandom.current().nextInt(startWords.size()));
		return output;
	}

	private void recordNewWords(String message) {

		String[] words = message.split(" ");
		if (!startWords.contains(words[0])) {
			if (words[0].contains("http")) {
				words[0] = "";
			}

			if (words[0].length() != 0) {
				if (words[0].charAt(0) != '!') {
					startWords.add(words[0]);
				}
			}
		}
		for (int i = 1; i < words.length - 1; i++) {
			if (chatWords.contains(words[i])) {
				continue;
			}
			if (words[i].contains("http")) {
				continue;
			}
			chatWords.add(words[i].replaceAll("[-+.^\"():,]!", ""));
		}

		if (!lastWords.contains(words[words.length - 1])) {
			lastWords.add(words[words.length - 1]);
		}
	}

	private BufferedImage genPortrait() {
		BufferedImage portrait = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) portrait.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 48, 48);
		g.drawImage(portraitHeadImages[ThreadLocalRandom.current().nextInt(portraitHeadImages.length)], null, 0, 0);
		g.drawImage(portraitArmorImages[ThreadLocalRandom.current().nextInt(portraitArmorImages.length)], null, 0, 0);
		g.drawImage(portraitHairImages[ThreadLocalRandom.current().nextInt(portraitHairImages.length)], null, 0, 0);
		g.drawImage(portraitMiscImages[ThreadLocalRandom.current().nextInt(portraitMiscImages.length)], null, 0, 0);

		BufferedImage dp = new BufferedImage(96, 96, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) dp.getGraphics();
		g2.drawImage(portrait, 0, 0, 96, 96, 0, 0, 48, 48, null);
		return dp;
	}

	private CharSequence generateName(int numGroups) {
		String name = "";
		String[] vowels = { "a", "e", "i", "o", "u" };
		String[] uncommonVowels = { "ou", "iou", "ae", "ea", "ai" };
		String[] consonants = { "b", "c", "d", "f", "g", "h", "l", "m", "n", "p", "r", "s", "t", "v" };
		String[] uncommonConsonants = { "j", "k", "q", "w", "x", "y", "z" };

		for (int i = 0; i < numGroups; i++) {
			if (ThreadLocalRandom.current().nextInt(100) < 90) {
				name += consonants[ThreadLocalRandom.current().nextInt(consonants.length)];
			} else {
				name += uncommonConsonants[ThreadLocalRandom.current().nextInt(uncommonConsonants.length)];
			}

			if (ThreadLocalRandom.current().nextInt(100) < 90) {
				name += vowels[ThreadLocalRandom.current().nextInt(vowels.length)];
			} else {
				name += uncommonVowels[ThreadLocalRandom.current().nextInt(uncommonVowels.length)];
			}

		}
		name += consonants[ThreadLocalRandom.current().nextInt(consonants.length)];

		return name.subSequence(0, 1).toString().toUpperCase() + name.substring(1);
	}

	private void generateNewCharacter(MessageReceivedEvent msg) {
		Character character = null;
		if (msg.getMessage().getContentRaw().split(" ").length > 1) {
			character = new Character(msg.getMessage().getContentRaw().split(" ")[1], RPGData);
		} else {
			character = new Character(msg.getAuthor().getName(), RPGData);
		}

		String output = "```";
		output += "Name:	     " + generateName(1) + " " + generateName(0) + " " + generateName(3) + "\n";
		output += "Strength:     " + character.strength + "\n";
		output += "Dexterity:    " + character.dexterity + "\n";
		output += "Constitution: " + character.constitution + "\n";
		output += "Intelligence: " + character.intelligence + "\n";
		output += "Wisdom:       " + character.wisdom + "\n";
		output += "Charisma:     " + character.charisma + "\n";
		output += "\n";
		output += "Background:   " + character.background + " - " + character.backgroundDesc;
		output += "\n\n";
		output += "Skills:\n";
		for (Entry<String, Integer> m : character.skills.entrySet()) {
			output += "\t" + m.getKey() + " - " + m.getValue() + "\n";
		}
		output += "```";

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(genPortrait(), "jpg", baos);
			baos.flush();
			byte[] imageByteArray = baos.toByteArray();
			baos.close();
			msg.getChannel().sendFile(imageByteArray, "Portrait.jpg").queue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		msg.getChannel().sendMessage(output).queue();
	}

	private static String generateCreature() {
		JSONObject creatureData = gameData.getJSONArray("Creature")
				.getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("Creature").length()));
		String output = "```\n";
		output += "type:\t\t" + getRandomValue(creatureData.getString("type")) + "\n";
		output += "Body:\t\t" + getRandomValue(creatureData.getString("body")) + "\n";
		output += "Limbs\t\t" + getRandomValue(creatureData.getString("limb")) + "\n";
		output += "Skin:\t\t" + getRandomValue(creatureData.getString("skin")) + "\n";
		output += "Weapon:\t\t" + getRandomValue(creatureData.getString("weapon")) + "\n";
		output += "Size:\t\t" + getRandomValue(creatureData.getString("size")) + "\n";
		output += "If Predator:\t" + getRandomValue(creatureData.getString("predator")) + "\n";
		output += "If Prey:\t\t" + getRandomValue(creatureData.getString("prey")) + "\n";
		output += "If Scavenger:\t" + getRandomValue(creatureData.getString("scavenger")) + "\n";
		output += "Discharge:\t" + getRandomValue(creatureData.getString("discharge")) + "\n";
		output += "Poison:\t\t" + getRandomValue(creatureData.getString("poison")) + "\n";
		output += "```";
		return output;
	}

	private static String generatePerson() {
		JSONObject problemData = gameData.getJSONArray("People")
				.getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("People").length()));
		String output = "```\n";
		output += "Manner:\t\t" + getRandomValue(problemData.getString("manner")) + "\n";
		output += "Outcome:\t" + getRandomValue(problemData.getString("outcome")) + "\n";
		output += "Motivation:\t" + getRandomValue(problemData.getString("motivation")) + "\n";
		output += "Want:\t\t" + getRandomValue(problemData.getString("want")) + "\n";
		output += "Power:\t\t" + getRandomValue(problemData.getString("power")) + "\n";
		output += "Hook:\t\t" + getRandomValue(problemData.getString("hook")) + "\n";
		output += "```";
		return output;
	}

	private static String generateProblem() {
		JSONObject problemData = gameData.getJSONArray("Problems")
				.getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("Problems").length()));
		String output = "```\n";
		output += "Type:\t\t" + getRandomValue(problemData.getString("type")) + "\n";
		output += "Situation:\t" + getRandomValue(problemData.getString("overall")) + "\n";
		output += "Focus:\t\t" + getRandomValue(problemData.getString("focus")) + "\n";
		output += "```";
		return output;
	}

	private static String generateWorld() {
		JSONObject worldData = gameData.getJSONArray("World")
				.getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("World").length()));
		String output = "```\n";
		output += worldData.getString("desc") + "\n";
		output += "Type:\t\t" + worldData.getString("tag") + "\n";
		output += "\n";
		output += "Enemy:\t\t" + getRandomValue(worldData.getString("enemies")) + "\n";
		output += "Friend:\t\t" + getRandomValue(worldData.getString("friends")) + "\n";
		output += "Complication:\t" + getRandomValue(worldData.getString("complication")) + "\n";
		output += "Thing:\t\t" + getRandomValue(worldData.getString("things")) + "\n";
		output += "Place:\t\t" + getRandomValue(worldData.getString("places")) + "\n";
		output += "\n";
		output += "Atmosphere:\t" + getRandomValue(gameData.getString("Atmosphere")) + "\n";
		output += "Temperature:\t" + getRandomValue(gameData.getString("Temperature")) + "\n";
		output += "BioSphere:\t" + getRandomValue(gameData.getString("BioSphere")) + "\n";
		output += "Population:\t" + getRandomValue(gameData.getString("Population")) + "\n";
		output += "TechLevel:\t" + getRandomValue(gameData.getString("TechLevel")) + "\n";
		output += "```";
		return output;
	}

	private static String getRandomValue(String obj) {
		String[] values = obj.split(",");
		String retVal = values[ThreadLocalRandom.current().nextInt(values.length)];
		if (retVal.startsWith(" ")) {
			return retVal.substring(1, retVal.length());
		}
		return retVal;
	}

	private static BufferedImage genMap() {
		int DPI = 300;
		// int CARD_SIZE = DPI * 3; // 3" hex
		// int CARD_WIDTH = 2 * CARD_SIZE;
		// int CARD_HEIGHT = (int) (Math.sqrt(3) * CARD_SIZE);
		// int CARD_BUFFER = DPI / 3;
		// int HEX_STEP = CARD_WIDTH / 4;

		int PRINT_HEX_SIZE = (int) (DPI * 0.8);
		int PRINT_HEX_WIDTH = 2 * PRINT_HEX_SIZE;
		int PRINT_HEX_HEIGHT = (int) (Math.sqrt(3) * PRINT_HEX_SIZE);
		int PRINT_HEX_STEP = PRINT_HEX_SIZE / 4;
		int PRINT_HEX_ELEV = 50;
		// int[] DEFENCE_VALUES = { 0, 1, 1, 1, 2, 2, 2, 2, 3, 3 };

		// Load images
		// Images and icons
		try {
			btn1 = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("button1.png"));
			btn2 = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("button2.png"));
			btn3 = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("button3.png"));
			btnA = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("buttonA.png"));
			btnB = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("buttonB.png"));
			exclamation = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("exclamation.png"));
			home = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("home.png"));
			ico_import = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("import.png"));
			power = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("power.png"));
			question = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("question.png"));
			singleplayer = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("singleplayer.png"));
			star = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("star.png"));
			target = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("target.png"));
			warning = ImageIO.read(SWNBOT.class.getClassLoader().getResourceAsStream("warning.png"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		Polygon hexShape = new Polygon();
		hexShape.addPoint(PRINT_HEX_STEP * 2, -PRINT_HEX_HEIGHT / 2);
		hexShape.addPoint(PRINT_HEX_WIDTH / 2, 0);
		hexShape.addPoint(PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2);
		hexShape.addPoint(-PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2);
		hexShape.addPoint(-PRINT_HEX_WIDTH / 2, 0);
		hexShape.addPoint(-PRINT_HEX_STEP * 2, -PRINT_HEX_HEIGHT / 2);

		Polygon hexElevation = new Polygon();
		hexElevation.addPoint(PRINT_HEX_WIDTH / 2, 0);
		hexElevation.addPoint(PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2);
		hexElevation.addPoint(-PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2);
		hexElevation.addPoint(-PRINT_HEX_WIDTH / 2, 0);

		hexElevation.addPoint(-PRINT_HEX_WIDTH / 2, PRINT_HEX_ELEV);
		hexElevation.addPoint(-PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2 + PRINT_HEX_ELEV);
		hexElevation.addPoint(PRINT_HEX_STEP * 2, PRINT_HEX_HEIGHT / 2 + PRINT_HEX_ELEV);
		hexElevation.addPoint(PRINT_HEX_WIDTH / 2, PRINT_HEX_ELEV);

		BufferedImage printMap = new BufferedImage(11 * DPI, (int) 8.5 * DPI, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) printMap.getGraphics();

		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, printMap.getWidth(), printMap.getHeight());

		int startY = 475;
		int hexSpacer = PRINT_HEX_WIDTH + PRINT_HEX_WIDTH / 2;
		g.translate(0, startY);
		AffineTransform startSpot = g.getTransform();
		List<MapLocation> elevatedHexes = new ArrayList<MapLocation>();

		long seed = System.currentTimeMillis();
		GeneratedMission gmiss = generateMission(seed);

		/**************************************************************
		 * Draw Hexes
		 ***************************************************************/
		for (int y = 0; y < gmiss.map.length; y++) {
			g.setTransform(startSpot);
			g.translate(0, PRINT_HEX_HEIGHT * y);
			for (int x = 0; x < gmiss.map.length; x++) {
				g.translate(hexSpacer / 2, 0);
				if (x % 2 != 0) {
					g.translate(0, PRINT_HEX_HEIGHT / 2);
				} else {
					g.translate(0, -PRINT_HEX_HEIGHT / 2);
				}
				if (gmiss.map[x][y].elevation > 0) {
					elevatedHexes.add(new MapLocation(gmiss.map[x][y], g.getTransform()));
				} else {
					g.setColor(Color.decode("#EEEEEE"));
					if (gmiss.map[x][y].isObjective) {
						g.setColor(Color.LIGHT_GRAY);
					}
					if (gmiss.map[x][y].isStartSpace) {
						g.setColor(Color.GRAY);
					}
					if (gmiss.map[x][y].isEnemyStartSpace) {
						g.setColor(Color.decode("#CCCCCC"));
					}
					g.fillPolygon(hexShape);
					g.setColor(Color.black);
					g.drawPolygon(hexShape);
					// g.setFont(fntTerrainCover);

					// Draw Icons
					switch (gmiss.map[x][y].defence) {
					case 1:
						g.drawImage(btn1, null, -50, -200);
						break;
					case 2:
						g.drawImage(btn2, null, -50, -200);
						break;
					case 3:
						g.drawImage(btn3, null, -50, -200);
						break;
					default:
						break;
					}

					if (gmiss.map[x][y].isEnemyStartSpace) {
						g.drawImage(warning, null, -50, 0);
					}
					if (gmiss.map[x][y].isObjective) {
						g.drawImage(star, null, -50, 0);
					}
					if (gmiss.map[x][y].isStartSpace) {
						g.drawImage(singleplayer, null, -50, 0);
					}
				}
			}
		}

		Comparator<MapLocation> ysort = (MapLocation o1, MapLocation o2) -> Double.compare(o1.transform.getTranslateY(),
				o2.transform.getTranslateY());
		Collections.sort(elevatedHexes, ysort);
		for (MapLocation hex : elevatedHexes) {
			g.setTransform(hex.transform);
			g.translate(0, -PRINT_HEX_ELEV);
			g.setColor(Color.decode("#EEEEEE"));
			if (hex.hex.elevation > 0) {
				g.setColor(Color.white);
			}
			if (hex.hex.isObjective) {
				g.setColor(Color.LIGHT_GRAY);
			}
			if (hex.hex.isStartSpace) {
				g.setColor(Color.GRAY);
			}
			if (hex.hex.isEnemyStartSpace) {
				g.setColor(Color.decode("#CCCCCC"));
			}
			g.fillPolygon(hexShape);
			g.setColor(Color.black);
			g.drawPolygon(hexShape);
			g.setColor(Color.DARK_GRAY);
			g.fillPolygon(hexElevation);
			g.translate(0, PRINT_HEX_ELEV);
			// g.setFont(fntTerrainCover);
			// Draw Icons
			switch (hex.hex.defence) {
			case 1:
				g.drawImage(btn1, null, -50, -200 - PRINT_HEX_ELEV);
				break;
			case 2:
				g.drawImage(btn2, null, -50, -200 - PRINT_HEX_ELEV);
				break;
			case 3:
				g.drawImage(btn3, null, -50, -200 - PRINT_HEX_ELEV);
				break;
			default:
				break;
			}

			if (hex.hex.isEnemyStartSpace) {
				g.drawImage(warning, null, -50, 0 - PRINT_HEX_ELEV);
			}
			if (hex.hex.isObjective) {
				g.drawImage(star, null, -50, 0 - PRINT_HEX_ELEV);
			}
			if (hex.hex.isStartSpace) {
				g.drawImage(singleplayer, null, -50, 0 - PRINT_HEX_ELEV);
			}
		}

		return printMap;
	}

	public static GeneratedMission generateMission(Long seed) {
		GeneratedMission gmiss = new GeneratedMission();

		gmiss.mission = MISSION_TYPE.values()[ThreadLocalRandom.current().nextInt(MISSION_TYPE.values().length)];
		// gmiss.mission = MISSION_TYPE.DEFEND;

		gmiss.overview = new AttributedString("Test overview");
		gmiss.primary = new AttributedString("test primary");
		gmiss.secondary = new AttributedString("test secondary");
		gmiss.setup = new AttributedString("setup");

		// generate map
		int mapWidth = 5;
		int mapHeight = 5;
		Hex[][] map = new Hex[mapWidth][mapHeight];
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				map[x][y] = new Hex();
				map[x][y].x = x;
				map[x][y].y = y;
				map[x][y].blocksLOS = (ThreadLocalRandom.current().nextInt(100) > 90) ? true : false;
				map[x][y].elevation = (ThreadLocalRandom.current().nextInt(100) > 75) ? 1 : 0;
				map[x][y].defence = DEFENCE_VALUES[ThreadLocalRandom.current().nextInt(DEFENCE_VALUES.length)];
			}
		}
		gmiss.map = map;

		// determine start locations
		switch (gmiss.mission) {
		case DEFEND:
			gmiss.requiredGroups = 3;
			gmiss.map[1][4].isStartSpace = true;
			gmiss.map[2][4].isStartSpace = true;
			gmiss.map[3][4].isStartSpace = true;
			gmiss.map[2][0].isStartSpace = true;
			gmiss.map[0][3].isEnemyStartSpace = true;
			gmiss.map[4][3].isEnemyStartSpace = true;
			break;
		case DESTROY_INFRASTRUCTURE:
			gmiss.requiredGroups = 3;

			gmiss.map[1][0].isObjective = true;
			gmiss.map[3][0].isObjective = true;

			gmiss.map[2][1].isEnemyStartSpace = true;

			gmiss.map[0][4].isStartSpace = true;
			gmiss.map[1][4].isStartSpace = true;
			gmiss.map[3][4].isStartSpace = true;
			gmiss.map[4][4].isStartSpace = true;

			break;
		case DOWNLINK:
			gmiss.map[0][1].isObjective = true;
			gmiss.map[4][1].isObjective = true;

			gmiss.map[0][0].isEnemyStartSpace = true;
			gmiss.map[4][0].isEnemyStartSpace = true;

			gmiss.map[0][4].isStartSpace = true;
			gmiss.map[1][4].isStartSpace = true;
			gmiss.map[3][4].isStartSpace = true;
			gmiss.map[4][4].isStartSpace = true;

			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		case HIT_AND_RUN:
			gmiss.requiredGroups = 3;
			gmiss.map[0][0].isStartSpace = true;
			gmiss.map[0][1].isStartSpace = true;
			gmiss.map[0][3].isStartSpace = true;
			gmiss.map[0][4].isStartSpace = true;

			gmiss.map[3][1].isObjective = true;
			gmiss.map[3][2].isObjective = true;

			gmiss.map[4][0].isEnemyStartSpace = true;
			gmiss.map[4][4].isEnemyStartSpace = true;

			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		case PERIMETER_CHECK:
			gmiss.requiredGroups = 3;
			gmiss.map[0][1].isObjective = true;
			gmiss.map[2][0].isObjective = true;
			gmiss.map[4][1].isObjective = true;

			gmiss.map[0][4].isStartSpace = true;
			gmiss.map[1][4].isStartSpace = true;
			gmiss.map[3][4].isStartSpace = true;
			gmiss.map[4][4].isStartSpace = true;

			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		case RESCUE:
			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		case SCOUT_AND_CLEAR:
			gmiss.requiredGroups = 2;

			gmiss.map[0][0].isStartSpace = true;
			gmiss.map[4][0].isStartSpace = true;
			gmiss.map[0][4].isStartSpace = true;
			gmiss.map[4][4].isStartSpace = true;

			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		case SECURE_AND_HOLD:
			gmiss.requiredGroups = 2;

			gmiss.map[0][0].isEnemyStartSpace = true;
			gmiss.map[4][4].isEnemyStartSpace = true;

			gmiss.map[4][0].isStartSpace = true;
			gmiss.map[0][4].isStartSpace = true;

			gmiss.map[2][2].isObjective = true;

			gmiss.primary = null;
			gmiss.secondary = null;
			gmiss.setup = null;
			break;
		}
		return gmiss;
	}

	private static String overwatch() {
		String characterList = "Junkrat";

		return "You should play: " + getRandomValue(characterList);
	}

	//////////////////////////////////////////////////////////////////////
	// IRONSWORN SECTION
	//////////////////////////////////////////////////////////////////////

	private void parseIronsworn(MessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		switch (args[1]) {
		case "roll":
			try {
				roll(Integer.valueOf(args[2]));
			} catch (Exception ex) {
				event.getChannel()
						.sendMessage("Error parsing command. Use !roll (action roll bonus). \n For example: !roll 2")
						.queue();
			}
			break;
		case "new":
			break;
		case "move":
			break;
		case "oracle":
			break;
		}
	}

	private CharSequence roll(int bonus) {
		String output = "";
		int actionRoll = ThreadLocalRandom.current().nextInt(6) + 1 + bonus;
		int numSuccess = 0;
		int[] challengeRolls = { 0, 0 };
		for (int i = 0; i < 2; i++) {
			challengeRolls[i] = ThreadLocalRandom.current().nextInt(10) + 1;
			if (actionRoll > challengeRolls[i]) {
				numSuccess++;
			}
		}

		output += "ROLLING D6+" + bonus + " against 2D10...";
		switch (numSuccess) {
		case 0:
			output += "\nFAIL - ";
			break;
		case 1:
			output += "\nWEAK HIT - ";
			break;
		case 2:
			output += "\nSTRONG HIT - ";
			break;
		}
		output += actionRoll + " vs " + challengeRolls[0] + ", " + challengeRolls[1];

		if (challengeRolls[0] == challengeRolls[1]) {
			output += " (match)";
		}

		return output;
	}

	private CharSequence settlementName() {
		String name = "";
		String[] prefix = { "Bleak", "Green", "Wolf", "Raven", "Gray", "Red", "Axe", "Great", "Wood", "Low", "White",
				"Storm", "Black", "Mourn", "New", "Stone", "Grim", "Lost", "High", "Rock", "Shield", "Sword", "Frost",
				"Thorn", "Long" };
		String[] suffix = { "moor", "ford", "crag", "watch", "hope", "wood", "ridge", "stone", "haven", "fall(s)",
				"river", "field", "hill", "bridge", "mark", "cairn", "land", "hall", "mount", "rock", "brook", "barrow",
				"stead", "home", "wick" };
		name = prefix[ThreadLocalRandom.current().nextInt(prefix.length)]
				+ suffix[ThreadLocalRandom.current().nextInt(suffix.length)];
		return name;
	}


	//////////////////////////////////////////////////////////////////////
	// HEART SECTION
	//////////////////////////////////////////////////////////////////////

	private CharSequence getFallout(FALLOUT_LEVEL level, FALLOUT_TYPE type) {
		String fallout = "";
		switch (level) {
		case CRITICAL:
			switch (type) {
			case BLOOD:
				String[] critBlood = {
						"BLEEDING OUT: You’re dying. Choose: do something useful before you die (and roll with mastery, because this is the last thing you’ll ever do) or desperately try to cling onto life (and lose something vital in the bargain). [Immediate]",
						"CHOSEN: You pass out and awaken in a halfdream state before the Heart; you have been blessed with its power. You return to life seemingly unharmed – a miracle! But within a session or two your transformation into an Angel, a nightmare creature of blistering unreality and scintillating ribbons of wet flesh, is complete. You’re not dead; but then again, you can’t die. You retire as a player character. [Ongoing]",
						"GHOST: You die, but your spirit doesn’t rest easy. Your ghost remains in the City Beneath, angry at the manner of its death and the friends who it believes failed it – you haunt the party. Until they lay your spirit to rest (or murder it with specialised weapons), they suffer D6 Mind or Echo stress at the start of every session. [Immediate]" };
				fallout = critBlood[ThreadLocalRandom.current().nextInt(critBlood.length)];
				break;
			case ECHO:
				String[] critEcho = {
						"BEAST: Your body warps and changes beyond recognition: you become a protean mess of meat and bone, terrifying to behold. You disappear into the Heart, surfacing only as a legend whispered in deep shadows; those who knew you tell stories of your exploits to remember you and warn others. You will be seen again in future sessions: the GM can use the stats for a Greater Heartsblood Beast to represent you. [Immediate]",
						"BURST: Unable to contain the energy (or parasites, or your alternate self) within your form any longer, your body ruptures like overripe fruit. If you’re lucky, you die; otherwise, you’re kept alive indefinitely, spread throughout the Heart. Anyone standing near you marks D6 stress to Echo.",
						"DESCENT: The next time you’re in a landmark, the ground shakes as the Heart draws you and the place further down. Move the landmark, and anyone in it, to the next available space on the tier below. This is catastrophic; most people will not survive. You are swallowed up by the Heart, and retire as a player character. [Immediate]",
						"MESSIAH: You have been followed by a cult of weirdos and monsters for long enough now – something’s got to give. One of two things happen: either you believe your own hype and become a cult leader, leaving your life of delving behind and attempting to set up shop in a new haven within the Heart, or the cult out you as a false prophet and attempt to kill you in an appropriately brutal and public manner. [Immediate]",
						"PETRIFIED: Your body calcifies, ossifies or crystalises, and you become a perfect statue of yourself. Your body is incredibly resistant to damage, so most people who turn into statues are left where they stand as a warning to others or as a sombre tribute.",
						"THE RAVENING: Your body hatches with a wet red noise and a Ravening Beast emerges from you, skinless and steaming with heat, hungry for food. You die; this is how they breed. If your surviving friends are nearby, see boxout on p. 85 for Ravening Beast combat information. [Immediate]",
						"STRANDED: Somewhere along the line, your reality diverged from everyone else’s – and by the time you realise, it’s too late to do anything about it. You are marooned in a fracture, an alternate future or a parasite dimension – tell the group about where you spend the rest of your life. You might return, but it’s doubtful that anyone you know will recognise you (or be alive) by the time you get back. [Immediate]" };
				fallout = critEcho[ThreadLocalRandom.current().nextInt(critEcho.length)];
				break;
			case FORTUNE:
				String[] critFortune = {
						"FOOL’S GOLD: You get exactly what you want – secrets, penitence, freedom, whatever. It seems too good to be true, which is apt, because it isn’t: you’ve been tricked or deluded, and you don’t have it after all. The stress is too much for you to bear, and your only worth now is as a warning to other delvers not to take things at face value. [Immediate]",
						"HEAVY HANGS THE HEAD: Despite your best efforts, you are elected leader of a haven; maybe you defend it from an attack and the people think you’ve got what it takes to run the place. You don’t. You have the capacity to do one useful thing with the haven’s resources before you’re assassinated by your rivals, the haven is overrun or the fact you were a patsy all along is revealed and you’re hung out to dry.[Immediate]",
						"A SLOW AND INSIDIOUS KILLER: You act with unearned confidence and your hubris is rewarded with an ironic death. [Immediate]",
						"WRONG PLACE: You accidentally take a fatal blow meant for someone else. They are unharmed; you die. [Immediate]" };
				fallout = critFortune[ThreadLocalRandom.current().nextInt(critFortune.length)];
				break;
			case MIND:
				String[] critMind = {
						"ABANDON: You have taken complete leave of your senses after the horrors that the Heart has laid upon you. You wander off into the wilderness; if you return, you will not be recognisable. More likely, you will starve to death in a cellar. [Immediate]",
						"BREAK: You completely lose it. Anyone standing nearby who you care about marks D8 stress to Mind; anyone standing nearby who you’ve never really liked marks D8 stress to Blood as you attack them. After this, you die (either self-inflicted or at the hands of your allies) or your mind is so shattered that you retire as a player character. [Immediate]",
						"OBSESSED: Your purpose has become twisted and cruel; you will stop at nothing to achieve it. Your character is retired from play and becomes an antagonist that acts against the surviving player characters in an effort to achieve their wicked desires. [Immediate, Ongoing]" };
				fallout = critMind[ThreadLocalRandom.current().nextInt(critMind.length)];
				break;
			case SUPPLIES:
				String[] critSupplies = {
						"ABANDON: You have taken complete leave of your senses after the horrors that the Heart has laid upon you. You wander off into the wilderness; if you return, you will not be recognisable. More likely, you will starve to death in a cellar. [Immediate]",
						"BREAK: You completely lose it. Anyone standing nearby who you care about marks D8 stress to Mind; anyone standing nearby who you’ve never really liked marks D8 stress to Blood as you attack them. After this, you die (either self-inflicted or at the hands of your allies) or your mind is so shattered that you retire as a player character. [Immediate]",
						"OBSESSED: Your purpose has become twisted and cruel; you will stop at nothing to achieve it. Your character is retired from play and becomes an antagonist that acts against the surviving player characters in an effort to achieve their wicked desires. [Immediate, Ongoing]" };
				fallout = critSupplies[ThreadLocalRandom.current().nextInt(critSupplies.length)];
				break;
			}
			break;
		case MAJOUR:
			switch (type) {
			case BLOOD:
				String[] majBlood = { "ARTERIAL WOUND: As BLEEDING,but you mark D6 stress at the end of every situation. [Ongoing]",
						"BLINDED: You can’t see, or can see so little that you might as well be blind. It might be permanent. Any task involving vision (so, most of them) becomes Dangerous. [Ongoing]" ,
						"BROKEN ARM: Your arm breaks under the strain, and splintered bone juts up through your skin. You can’t use the arm until it heals, which will make some tasks Risky or Dangerous, and others impossible. This fallout can be downgraded to or upgraded from BATTERED. [Ongoing]" ,
						"BROKEN LEG: Your leg bones splinter and crack. Any action involving the leg (climbing, moving above a crawl) automatically fails. This fallout can be downgraded to or upgraded from LIMPING. [Ongoing]" ,
						"CRITICAL INJURY: You take a hit somewhere vital. The GM picks a skill you have access to and you no longer have access to that skill. For example, a hit to your sword-arm could remove Kill; an eye injury could remove Discern; ripped tendons in your fingers could remove Mend. [Ongoing]", 
						"DOWNED: You can’t move under your own power and you’re barely clinging on to consciousness. You can be moved around by others, but without medical attention, you’re not going anywhere. Can be upgraded to DYING. [Ongoing]" ,
						"EXHAUSTED: You can’t go on; if you push yourself any harder you’re going to pass out. Stop now, or convince someone else to carry you the rest of the way. Alternatively, make one more action and then fall unconscious once the roll is resolved. [Immediate, Ongoing]" };
				fallout = majBlood[ThreadLocalRandom.current().nextInt(majBlood.length)];
				break;
			case ECHO:
				String[] majEcho = { "BLOODED: You show some mark of the Heart in your physical form: twisting, fragile antlers of bone, fingernails that curve in fractal-sharp patterns, bioluminescent veins, additional joints in your limbs, and so on. Your frail mortal form is not designed to be used as such a canvas; when you mark stress to Blood, roll two dice and pick the higher. [Ongoing]", 
						"CULT: Your actions mark you as a true scion of the Heart, and weird people/creatures will trail around you, espousing your glories (whether real or imagined). This is nothing but trouble, and any attempt to take advantage of them will go wrong. Getting into a haven will be difficult with this many people around, so you’d best hope that some of them die along the way. [Ongoing]", 
						"DARK CRAVINGS: As STRANGE APPETITE, but the effect is permanent until this fallout is removed. [Ongoing]", 
						"EYES: Your eyes become wide black orbs; or perhaps you find more eyes blossoming on your body, growing in your sternum like a nest of spiders. You can see perfectly well in the dark, but lights dazzle and hurt you. The GM can call for an Endure check when you enter a well-lit area, and you take stress on a failure or partial success. [Ongoing]", 
						"THE LIFE NOT LIVED: Upgrades DEJA VU. You meet someone from your past who should, by all rights, be dead. [Immediate]", 
						"MEAT: Everyone is just meat to you: dull, worthless, soulless. Any time you enter a situation where you must talk to a mundane NPC for an extended period of time, the GM can call for an Endure roll; on a failure, take D6 stress. If you are intimate with a mundane NPC, take D10 stress on a failure. [Ongoing]", 
						"MIRAGE: The next landmark you reach is a facsimile made by the Heart, arranged to give you what you want. It seems real, but the more you explore, the more obvious it is that everything – the streets, the books, the people – is fake. It’s an artful copy made out of meat, bone and blood. From the looks of things, it’s existed for hundreds of years. Once you realise that the landmark is fake, remove this fallout. [Immediate]", 
						"THE RAVENING BEAST: Emerging from a patch of shadows, the Ravening Beast that has been hiding in your mind appears. It will attempt to maul others, but its primary motivation is to consume you utterly. Fighting off the beast does not remove this fallout, but it has no further effect unless you upgrade this fallout (see THE RAVENING below). [Immediate]", 
						"RECONFIGURED PHYSIOLOGY: Your organs and bones don’t make sense any more. You can no longer remove stress from Blood, or remove Blood fallout at haunts or through the use of medical kits.This shows itself in some outward fashion – unusual growths bulging under your skin, words appearing as bruises, black blood and so on. [Ongoing]", 
						"VANISHED: The next landmark you reach isn’t there; you find something else instead. Presumably the landmark is somewhere, assuming it hasn’t been entirely swallowed up by the Heart. [Immediate]" };
				fallout = majEcho[ThreadLocalRandom.current().nextInt(majEcho.length)];
				break;
			case FORTUNE:
				String[] majFortune = { "CRISIS: As FOREBODING, above, but now the danger actually occurs. [Immediate]", 
						"DESTROYED: If you’re currently on a delve that has a connection established, remove the connection and describe what happened. If you’re inside a landmark, you destroy something of value and remove one of the landmark’s haunts (if it has any). Tell us how you did it (on purpose or accidentally). [Immediate]", 
						"EXILED: You are banned from entering the haven you are currently in, or one that’s nearby – tell us what happened. Your allies aren’t subject to the same restrictions, but they will be treated with suspicion. [Immediate, Ongoing]", 
						"GRIEVANCE: You are marked as an enemy by a group within the Heart – a cult, a church, the Hounds, members of a particular haven, beasts, etc.They will work to foil your efforts as best they can until you clear your name or kill your way out of the problem. [Immediate, Ongoing]", 
						"HELL FOR WEATHER: Your predictions were wrong and you lead the party into an actively dangerous area; or, the area you’re in changes and becomes hostile; or, if the locale permits, a storm whips up. Until you reach a landmark, all actions the party take become Risky. Remove this fallout when you do. [Immediate, Ongoing]", 
						"LOST MAP: Your map is lost or stolen. Until you get it back or replace it, you cannot use connections – every journey is treated as unexplored territory. [Ongoing]", 
						"LOST PROPERTY: You have misplaced an item; the GM picks which. You could spend time searching for it, but you’ll need to back-track – and someone might have made off with it already. [Immediate]", 
						"NO WAY OUT: You lead the party into a dead-end, a trap or an ambush. Remove this fallout once you get out alive. [Immediate]", 
						"REPUTATION: Another delver has gotten wind of your successes (or your weaknesses) and they are coming for you. You are ambushed by a hunter seeking to claim your head and relieve you of your hard earned supplies. Remove this fallout once the fight is over. [Immediate]", 
						"THE ROAD LESS TRAVELLED: You pick the wrong path. The next landmark you reach isn’t the one you’re expecting – it’s an entirely different one, and probably one you were trying to avoid. [Immediate]", 
						"UNWILLING LEADER: You end up responsible for an unwanted group – they have problems and they look to you to solve them. Maybe you killed their boss and now you’re the new boss; maybe they appeal to your sense of kindness and you foolishly give in; maybe the “group” is an orphan you have to look after. You can use the group to achieve things, but honestly, they cause more problems than they solve. [Immediate, Ongoing]" };
				fallout = majFortune[ThreadLocalRandom.current().nextInt(majFortune.length)];
				break;
			case MIND:
				String[] majMind = { "AETHERIC RESONANCE: As COLLATERAL MAGIC Minor fallout, but you permanently learn AETHERIC SCOURGE and can cast it at will. [Ongoing]", 
						"ADDICT: You realise that you have become reliant on drugs to keep yourself stable. When you’re high (or drunk, or whatever) you take Mind stress normally, but all tasks that require extended concentration or fine manipulation become Risky. When you’re not intoxicated and you suffer stress to Mind, roll twice and pick the higher dice. It takes a few minutes to get high and a few hours to sober up. [Ongoing]", 
						"DELUSION: Something you believe to be true is in fact false. While you step outside, or during downtime, all the other players work with the GM to determine what you are deluded about. For example: you’re not an orphan, and you’ve got a family back in High Rise; there’s no such group as the Hounds, and your uniform doesn’t mean anything; etc. Next time you encounter the subject, reality (and the other characters) behave appropriately, leaving you confused and shaken. ", 
						"DESPAIR: Your mind races with the implications of what you’ve seen; your life before seems unreal and distant. The GM picks a domain that you have access to and you no longer have access to that domain. [Ongoing]", 
						"MEMORY HOLES: You did things that you can’t quite recall. While you step outside, or during downtime, all the other players work with the GM to determine what you did that you blocked from your mind. These are generally pretty awful things. They can have happened up to a year ago in game time, or immediately upon suffering fallout. Your character has zero memory of the events, but everyone else involved knows what happened. [Immediate]", 
						"PHANTASM: As FIGMENT (minor), but the GM chooses a Major fallout instead. [Ongoing]", 
						"SCARRED: (See SHAKEN minor) Your mind cracks and reforms in primal, instinctive patterns. This functions as SHAKEN. In addition, every time you encounter the source of the fallout from now on, the GM can ask you to make an Endure check or suffer D8 stress to Mind. [Ongoing]", 
						"UNSETTLING: (See SHAKEN minor) You behave in a weird manner, causing your companions discomfort. This functions as SHAKEN, and any friendly character who sees you perform the act takes D6 Mind stress. [Immediate]" };
				fallout = majMind[ThreadLocalRandom.current().nextInt(majMind.length)];
				break;
			case SUPPLIES:
				String[] majSupplies = { "IN THE DARK: Your torch sputters out, and you can’t re-light it. As DARKNESS, above, but someone or something also takes the opportunity to strike while you’re vulnerable. Until you can get some light, the fight is Dangerous. Once things calm down, another party member can remove this fallout by marking D6 stress to Supplies. [Immediate, Ongoing]", 
						"LOST PROPERTY: You have misplaced an item; the GM picks which. You could spend time searching for it,but you’ll need to back-track –and someone might have made off with it already. [Immediate]", 
						"NO RATIONS: You’re out of food. This functions as HALF RATIONS, and all actions you make become Risky due to shaking hands and low blood sugar. Another party member can remove this fallout by marking D8 stress to Supplies. [Ongoing]", 
						"SERVICES RENDERED: You’ve been forced to sell your skills to a third party to pay your debtors, and the work is not pleasant. Work out with the GM what your character doesn’t want to do but is prepared to do to make ends meet. If you don’t do it, you’ll be in trouble; alternatively, you’ve done it in the past, and you describe it in a flashback. [Immediate]", 
						"SOLD: You’re forced to sell off something valuable to pay your debtors. Work out with the GM what you’re forced to sell. If you haven’t used it since you last visited a haven, you sold it retroactively. [Immediate]", 
						"SPOILED: A resource you are carrying is destroyed: it degrades into uselessness, is revealed to be fake, breaks in your pack or scatters on the ground. Remove it from your possessions. [Immediate]" };
				fallout = majSupplies[ThreadLocalRandom.current().nextInt(majSupplies.length)];
				break;
			}
			break;
		case MINOR:
			switch (type) {
			case BLOOD:
				String[] minorBlood = {
						"BATTERED: Your dominant hand is injured; you can bandage it up and stop the bleeding, but it’s of limited use for the time being. Any offensive action you make in combat becomes Risky; any tasks that require fine dexterity are out of the question. [Ongoing]",
						"BLEEDING: At the end of each situation where you have this fallout, mark D4 stress to Blood. [Ongoing]",
						"DISARMED: You drop and lose whatever you’re holding, leaving you defenceless; you inflict D4 stress in combat until you source a weapon. If you’re somewhere precarious, you might lose the item forever. [Immediate]",
						"FURIOUS: You’re hurt, short-tempered and perceive sleights everywhere. You cannot help another character by adding a dice to their roll. [Ongoing]",
						"LIMPING: You’re slowed. If someone or something attacks your party, they’ll attack you first. If there’s any question over who arrives last, it’s you. All checks involving rapid or stealthy movement become Risky. [Ongoing]",
						"RINGING HEAD: Your head swims and you taste blood in your mouth. The next action you take is Dangerous, the one after that is Risky, and then you remove this fallout as your head clears. [Immediate]",
						"SHATTERED: [also: Supplies] Your armour is no longer of use. You cannot use Blood Protection.[Ongoing]",
						"SPITTING TEETH: Any action that requires you to speak or look respectable is Risky. [Ongoing]",
						"TIRED: You’re weary; you’re going to make bad decisions and snap at your friends. You cannot gain extra dice from skills. [Ongoing]",
						"WINDED: When you attack with melee weapons, decrease stress inflicted by one dice size.[Ongoing]" };
				fallout = minorBlood[ThreadLocalRandom.current().nextInt(minorBlood.length)];
				break;
			case ECHO:
				String[] minorEcho = {
						"BUBOES: Your skin blisters and bubbles. When you take Blood stress, take an extra D4 as the boils split and burst. [Ongoing]",
						"CONDUIT: Your best efforts to keep the unreal energies of the Heart at bay are futile: your body is a crucible for strangeness. You cannot use Echo protection. [Ongoing]",
						"DEJA VU: You notice minor elements of your past life appearing in the Heart as though it is reading your mind and adapting itself to your expectations; the GM tells you what happens. [Immediate, Ongoing]",
						"EXODUS: You retch up a handful of writhing creatures: pallid fat moths, translucent grubs, spiders with the wrong number of legs, throbbing parasites and so on. Anyone who sees this and isn’t ready for it marks D4 stress to Mind.[Immediate]",
						"FOLLOWER: Someone, or something, believes you are very important: chosen of the Heart and worth following. A weird-looking but essentially harmless creature or person follows you at a distance; they won’t approach you, but they’ll be keen to sift through your rubbish or attempt to hurt anyone who looks like they might want to get in your way. [Ongoing]",
						"GLITCH: You disappear, only reappearing after every other player has acted at least once.[Immediate]",
						"HEX-EYE: Your vision swims as you start to perceive worlds other than your own layered on top of one another. Any action you take that requires accurate judging of distance (jumping, shooting, running down a corridor) becomes Risky. Once per session, you see something useful – ask the GM what it is. [Ongoing]",
						"THE RAVENING CALL: This fallout has no effect, but it stays on your character, and occasionally manifests as a broken, staccato howl in the back of their mind. Should this fallout by upgraded,see THE RAVENING BEAST below. [Ongoing",
						"STRANGE APPETITE: You crave unusual – taboo – things rather than good honest meat and drink: rusted metal, living creatures, vermin, effluvia, used clothing, beloved pets, etc. Next time you visit a haunt and attempt to refresh Blood or Mind stress, you must seek out this weirdness and indulge in it; otherwise you will be unable to refresh. Once you’ve sought out the weirdness, remove this fallout. This fallout can be upgraded into DARK CRAVINGS. [Ongoing]",
						"SIREN SONG: You cannot shake the thought of a particular place or person from your mind (the GM will pick a nearby landmark or NPC). If you do anything other than move towards it or remove obstacles in your path, the action becomes Risky. Once you reach it, the feeling dissipates. [Ongoing]" };
				fallout = minorEcho[ThreadLocalRandom.current().nextInt(minorEcho.length)];
				break;
			case FORTUNE:
				String[] minorFortune = {
						"BROKEN: An important item is damaged. You cannot use it until you take the time and resources to repair it. [Ongoing]",
						"COLLATERAL: The next time you mark stress, a nearby ally marks the same amount; you then remove this fallout. [Immediate]",
						"FOREBODING: Something bad is about to happen. GM, hint at an ominous future event – smoke in the distance, the tremors before a pulse, the frantic music of the Carnival. This fallout can be upgraded to CRISIS (Majour fallout).[Ongoing]",
						"THE HARD WAY: You lead the party into danger. The next obstacle the group attempts to overcome is Dangerous; they can reduce it to Risky, or even Standard, with a decent plan. [Immediate]",
						"IN TROUBLE: You upset an important figure in this or a nearby haven. [Immediate, Ongoing]",
						"LONG WAY ROUND: You take longer than expected to reach your target. On a delve, add D6 to the delve’s resistance. If you’re searching for someone or something in a landmark, you arrive just late enough to be in trouble.[Immediate]",
						"SEPARATED: You think you hear something, but when you turn to tell your allies, they are gone. You’ll have to track them down or hope they find you. [Immediate]",
						"UNLUCKY: Things are going to get worse before they get better. You cannot use Fortune protection. [Ongoing]",
						"WORD OF MOUTH: Word spreads of your misdeeds. Wherever you’re headed next, someone knows you’re going there, and is going to try to take advantage of you. The GM shows how word is spreading. [Immediate]" };
				fallout = minorFortune[ThreadLocalRandom.current().nextInt(minorFortune.length)];
				break;
			case MIND:
				String[] minorMind = {
						"CLOUDED: Your mind starts to shut down in an attempt to protect itself; you can’t think straight and sensations are dulled. You cannot gain extra dice from Domains. [Ongoing]",
						"CREEPY: You react in a strange way that weirds out your friends – tell us how. Any friendly character who sees you do it marks D4 stress to Mind. [Immediate]",
						"COLLATERAL MAGIC: Your panicked mind breaks for a second and reforms in an arcane pattern; down here, the old magics of blood and bone work better than they do on the surface. You immediately cast AETHERIC SCOURGE on a nearby ally, but mark no stress for doing so. This fallout can be upgraded (see AETHERIC RESONANCE below). [Immediate]  AETHERIC SCOURGE: Mark D6 stress to cast this spell. A nearby target takes D6 stress as raw magic boils out of you and into them, burning their skin and hair.",
						"FASCINATION: You become obsessed with a strange topic – usually whatever caused the fallout. You must try and learn more about it, first hand if possible. Whenever you attempt to learn more about your weird fascination, roll with mastery. If you have the opportunity to learn about it and you refuse, mark D4 stress to Mind. [Ongoing]",
						"FIGMENT: You lose track of what’s real and what’s not. The GM picks a Minor fallout from a different resistance and tells you have it. Until this fallout is removed, you’re convinced you’re suffering from the fallout (no matter what others tell you) and suffer from all appropriate effects. (GM: it is up to you whether you inform the player that this is a delusion or not.) [Ongoing]",
						"SHAKEN: You panic and fall back on your primitive impulses. The GM chooses one: Fight (attack the problem in an attempt to destroy it), Flight (get away from the problem by any means necessary) or Freeze (do not act, putting yourself in danger). At the end of the situation, remove this fallout. [Immediate]",
						"TAKE THE EDGE OFF: You can’t get your head right until you have a drink (or something stronger). Until you reach a landmark with access to intoxicants and render yourself insensible, roll two dice when you mark stress to Mind and pick the higher. Can be upgraded to ADDICT. [Ongoing]",
						"VULNERABLE: You feel small, shaken and scared. You cannot use Mind protection. [Ongoing]",
						"WEIRD: You do something unsettling that bothers normal people – obsessive behaviour, singing to yourself, fulfilling a strange compulsion at inappropriate times. At the earliest opportunity, the GM can declare that your weirdness puts a useful NPC off you (and probably your allies, too). [Immediate, Ongoing]" };
				fallout = minorMind[ThreadLocalRandom.current().nextInt(minorMind.length)];
				break;
			case SUPPLIES:
				String[] minorSupplies = {
						"BROKEN: An important item is damaged. You cannot use it until you take the time and resources to repair it. [Ongoing]",
						"DAMAGED: A resource you’re carrying is defective in some way – dented, torn, scuffed or cracked. Reduce its dice size by 1 step. [Immediate]",
						"DARKNESS: Your supplies of spireblack oil run low. All Delve or Discern checks you make become Risky. Another party member can remove this fallout by marking D6 stress to Supplies. [Ongoing]",
						"DEBTOR: During the next session or later in this one, an NPC who lent you money will call in a favour. [Immediate]",
						"EMPTY: You’re down to your last scraps of food, your last scraping of spireblack. You cannot use Supplies protection. [Ongoing]",
						"HALF RATIONS: You’re running low on food.When you remove stress, roll two dice and pick the lowest of the two. Another party member can remove this fallout by marking D6 stress to Supplies.[Ongoing]",
						"OUT OF AMMO. You run out of ammunition for a ranged or powered weapon and it can no longer be used. If another party member has a similar weapon, they can remove this fallout by marking D6 stress to their own Supplies. [Ongoing]",
						"USED UP: Your stocks are depleted of crucial items, something has spoiled or someone’s stolen something vital from your bag. You cannot use any healing items you own. [Ongoing]" };
				fallout = minorSupplies[ThreadLocalRandom.current().nextInt(minorSupplies.length)];
				break;
			}
			break;
		}
		return fallout;
	}

	private CharSequence getResource() {
		String[] value = {"D4","D4","D4","D4","D4","D6","D6","D8","D10","D12" };
		String[] domain = {"Cursed", "Desolate", "Haven","Haven", "Occult", "Religion","Technology","Warrern","Wild"};
		String resource = "" + value[ThreadLocalRandom.current().nextInt(value.length)] + " " + domain[ThreadLocalRandom.current().nextInt(domain.length)];
		return resource;
	}
	
	private CharSequence getTechBabble() {
		String val = "";
		String[] adj = {"Postive", "Negative", "Hyper-", "Sub-", "Quantum", "Micro-", "Modulating", "Multi-", "Crossover", "Auxillary", "Perpetual", "Stable","Phase", "Primary", "Invasive", "Abnormal", "Starboard", "Port", "Secondary"};
		String[] noun1 = {"positron", "proton", "tachyon", "particles", "emissions", "pulse", "subspace", "baryon", "polarity", "ion", "plasma", "graviton", "signature", "wave", "arc", "dark energy", "solar", "vibration", "quark", "neutron"};
		String[] noun2 = {"warp", "module", "processor", "inhibitor", "controller", "injector", "drive", "system", "energy", "regulator", "conduit", "dispenser", "replicator", "barrery", "outlet", "manifold", "link", "shell", "monitor", "filter"};
		String[] good = {"activate", "bypass", "calculate", "compensate", "compile", "convert", "couple", "decontaminate", "detect", "divert", "stabilize", "enhance", "equalize","embiggen", "generate", "modulate", "probe", "radiate", "react to", "reroute", "reverse", "scan", "emit", "synchronize", "trim"};
		String[] bad = {"a collision","contamination","corruption", "a crack", "decay", "destabilization", "a disconnection", "disruption", "distorion", "a failure", "a flood", "a fuse", "a jam", "a leak", "a misfire", "a rupture", "slippage", "a stall", "uncoupling" };
		// THERE IS [bad] in the [adj] [n1] [n2]!
		// WE HAVE TO [good] the [adj] [n1] [n2]
		val += "There is " + bad[ThreadLocalRandom.current().nextInt(bad.length)] + " in the " + adj[ThreadLocalRandom.current().nextInt(adj.length)] + " " + noun1[ThreadLocalRandom.current().nextInt(noun1.length)] + " " + noun2[ThreadLocalRandom.current().nextInt(noun2.length)] + "\n";
		val += "You need to " + good[ThreadLocalRandom.current().nextInt(good.length)] + " the " + noun1[ThreadLocalRandom.current().nextInt(noun1.length)] + " " + noun2[ThreadLocalRandom.current().nextInt(noun2.length)];
		return val;
	}
}
