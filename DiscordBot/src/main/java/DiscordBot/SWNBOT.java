package DiscordBot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import DiscordBot.GeneratedMission.MISSION_TYPE;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import opennlp.tools.cmdline.langdetect.LanguageDetectorTrainerTool;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SWNBOT extends ListenerAdapter {

	private static String BOT_TOKEN = "NjEwNDY4NjEwMDYxODkzNjc0.XVFuZg.BengnVzHG0z4ugFXxiVRqmK5AJw";
	
	private static List<String> chatWords;
	private static List<String> startWords;
	private static List<String> lastWords;
	private static List<MessageChannel> channels;
	
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
	
	private static long EMOTE_GROSS = 377872040553611264L;
	
	public static int[] DEFENCE_VALUES = { 0, 1, 1, 1, 2, 2, 2, 2, 3, 3 };

	public static void main(String[] args) throws LoginException {
		loadData();
		chatWords = new ArrayList<String>();
		startWords = new ArrayList<String>();
		lastWords = new ArrayList<String>();
		loadMessageHistory();
		channels = new ArrayList<MessageChannel>();
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(BOT_TOKEN);
		builder.addEventListener(new SWNBOT());
		builder.buildAsync();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		
		if(!channels.contains(event.getChannel())) {
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
		for(Message msg : messages) {
			if(!msg.getAuthor().isBot()) {
				recordNewWords(msg.getContentRaw());
			}
		}
	}
	
	private static void nlpParser(String sentence)
	{
		InputStream is;
		try {
			is = new FileInputStream("D:\\Bot\\trained\\en-parser-chunking.bin");		
			ParserModel model = new ParserModel(is); 
			Parser parser = ParserFactory.create(model); 
			Parse[] topParses = ParserTool.parseLine(sentence, parser, 1); 
			for (Parse p : topParses) 
		         System.out.println(p.toString());
		} catch (Exception e) {
			e.printStackTrace();
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
		
		for(int i = 0; i < portraitArmourFiles.size(); i++) {
			try {
				portraitArmorImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Armour/" + portraitArmourFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < portraitHairFiles.size(); i++) {
			try {
				portraitHairImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Hair/" + portraitHairFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < portraitHeadFiles.size(); i++) {
			try {
				portraitHeadImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/Head/" + portraitHeadFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < portraitMiscFiles.size(); i++) {
			try {
				portraitMiscImages[i] = ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream("portrait/misc/" + portraitMiscFiles.get(i)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	private void parseMessage(MessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		try {
		if(args[0].charAt(0) != '!') {
			if(!event.getAuthor().isBot()) {
				recordNewWords(event.getMessage().getContentRaw());
			}
			return;
		}} catch (Exception ex) {
			System.out.println("Unable to parse message '" +  args[0] + "'");
		}
		switch (args[0]) {
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
		case "!name":
			try {
				if(args.length < 2) {
					event.getChannel().sendMessage(generateName(Integer.valueOf(args[1]))).queue();
				} else {
					String name = "";
					for(int i = 1; i < args.length; i++) {
						name += generateName(Integer.valueOf(args[i]));
						if(Integer.valueOf(args[i]) == 0) { name +=".";}
						name  += " ";
					}
					event.getChannel().sendMessage(name).queue();
				}
			} catch (Exception ex) {
				System.out.println("unable to parse arg: " + args[1]);
				ex.printStackTrace();
				event.getChannel().sendMessage("Error parsing command. Use !name (number of syllables) [optional](number of syllables)").queue();
			}
			break;
		case "!ttsname":
			try {
				if(args.length < 2) {
					event.getChannel().sendMessage(generateName(Integer.valueOf(args[1]))).queue();
				} else {
					String name = "";
					for(int i = 1; i < args.length; i++) {
						name += generateName(Integer.valueOf(args[i]));
						if(Integer.valueOf(args[i]) == 0) { name +=".";}
						name  += " ";
					}
					MessageBuilder mb = new MessageBuilder();
					mb.setTTS(true);
					mb.setContent(name);
					event.getChannel().sendMessage(mb.build()).queue();
				}
			} catch (Exception ex) {
				System.out.println("unable to parse arg: " + args[1]);
				ex.printStackTrace();
				event.getChannel().sendMessage("Error parsing command. Use !name (number of syllables) [optional](number of syllables)").queue();
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
			} catch(Exception e) {
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
			} catch(Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		case "!save":
			try {
				saveMessageHistory();
				event.getChannel().sendMessage("Message history saved.").queue();
			} catch(Exception e) {
				e.printStackTrace();
				event.getChannel().sendMessage("Unable to generate string. " + e.getMessage()).queue();
			}
			break;
		default:
			//SnowflakeCacheView<Emote> cache = event.getGuild().getEmoteCache();
			event.getChannel().addReactionById(event.getMessageId(), event.getGuild().getEmoteById(EMOTE_GROSS)).queue();
			break;
		}
	}

	private void saveMessageHistory() {
		try {
			FileWriter writer = new FileWriter("output.txt");
			for(String word : chatWords) {
				writer.append(word + "\n");
			}
			writer.close();
			
			writer = new FileWriter("startWords.txt");
			for(String word : startWords) {
				writer.append(word + "\n");
			}
			writer.close();
			
			writer = new FileWriter("lastWords.txt");
			for(String word : lastWords) {
				writer.append(word + "\n");
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void loadMessageHistory() {
		try {
			Scanner scanner = new Scanner("output.txt");
			scanner.hasNext();
			while(scanner.hasNext()) {
				chatWords.add(scanner.next());
			}
			scanner.close();
			
			scanner = new Scanner("startWords.txt");
			scanner.hasNext();
			while(scanner.hasNext()) {
				startWords.add(scanner.next());
			}
			scanner.close();
			
			scanner = new Scanner("lastWords.txt");
			scanner.hasNext();
			while(scanner.hasNext()) {
				lastWords.add(scanner.next());
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
		for(int i  = 0; i < numWords; i++) {
			output += chatWords.get(ThreadLocalRandom.current().nextInt(chatWords.size())) + " ";			
		}
		output += lastWords.get(ThreadLocalRandom.current().nextInt(startWords.size()));
		return output;
	}

	private void recordNewWords(String message) {
		String[] words = message.split(" ");
		if (!startWords.contains(words[0])) {
			if(words[0].length() != 0) {
				if(words[0].charAt(0) != '!') {
					startWords.add(words[0]);
				}
			}
		}
		for(int i = 1; i < words.length -1; i++) {
			if(chatWords.contains(words[i])) {
				continue;
			}
			chatWords.add(words[i].replaceAll("[-+.^\"():,]", ""));
		}
		
		if(!lastWords.contains(words[words.length-1])) {
			lastWords.add(words[words.length-1]);
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
		String[] vowels = {"a", "e", "i", "o", "u"};
		String[] uncommonVowels = { "ou", "iou", "ae", "ea", "ai"};
		String[] consonants = {"b","c","d","f","g","h","l", "m","n","p","r", "s", "t", "v"};
		String[] uncommonConsonants = {"j","k","q", "w", "x", "y", "z"};
				
		for(int i = 0; i < numGroups; i++) {
			if(ThreadLocalRandom.current().nextInt(100) < 90) {
				name += consonants[ThreadLocalRandom.current().nextInt(consonants.length)];
			} else {
				name += uncommonConsonants[ThreadLocalRandom.current().nextInt(uncommonConsonants.length)];
			}
			
			if(ThreadLocalRandom.current().nextInt(100) < 90) {
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
		if(msg.getMessage().getContentRaw().split(" ").length > 1) {
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
		output += "Wisdom:       " + character.wisdom+ "\n";
		output += "Charisma:     " + character.charisma + "\n";
		output += "\n";
		output += "Background:   " + character.background + " - " + character.backgroundDesc;
		output += "\n\n";
		output += "Skills:\n";
		for(Entry<String, Integer> m : character.skills.entrySet()) {
			output += "\t"+m.getKey() + " - " + m.getValue() + "\n";
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
		JSONObject creatureData = gameData.getJSONArray("Creature").getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("Creature").length()));
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
		JSONObject worldData = gameData.getJSONArray("World").getJSONObject(ThreadLocalRandom.current().nextInt(gameData.getJSONArray("World").length()));
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
		//int CARD_SIZE = DPI * 3; // 3" hex
		//int CARD_WIDTH = 2 * CARD_SIZE;
		//int CARD_HEIGHT = (int) (Math.sqrt(3) * CARD_SIZE);
		//int CARD_BUFFER = DPI / 3;
		//int HEX_STEP = CARD_WIDTH / 4;

		int PRINT_HEX_SIZE = (int) (DPI * 0.8);
		int PRINT_HEX_WIDTH = 2 * PRINT_HEX_SIZE;
		int PRINT_HEX_HEIGHT = (int) (Math.sqrt(3) * PRINT_HEX_SIZE);
		int PRINT_HEX_STEP = PRINT_HEX_SIZE / 4;
		int PRINT_HEX_ELEV = 50;
		//int[] DEFENCE_VALUES = { 0, 1, 1, 1, 2, 2, 2, 2, 3, 3 };

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
		//gmiss.mission = MISSION_TYPE.DEFEND;

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
}
