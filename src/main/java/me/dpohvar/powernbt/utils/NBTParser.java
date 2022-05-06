package me.dpohvar.powernbt.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import me.dpohvar.powernbt.api.NBTCompound;
import me.dpohvar.powernbt.api.NBTList;
import me.dpohvar.powernbt.api.NBTManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
import static java.util.stream.Collectors.toCollection;

public class NBTParser {

	private static final Pattern b = Pattern.compile("\\[[-+\\d|,\\s]+\\]");

	static int getType(String var) throws RuntimeException {
		int type = 0;
		boolean useQuote = false;
		LinkedList<Character> chars = new LinkedList<>();

		for (int i = 0; i < var.length(); ++i) {
			char c = var.charAt(i);
			if (c == '"') {
				if (b(var, i)) {
					if (!useQuote) {
						throw new RuntimeException("Illegal use of \\\": " + var);
					}
				} else {
					useQuote = !useQuote;
				}
			} else if (!useQuote) {
				if (c != '{' && c != '[') {
					if (c == '}' && (chars.isEmpty() || chars.pop() != '{')) {
						throw new RuntimeException("Unbalanced curly brackets {}: " + var);
					}

					if (c == ']' && (chars.isEmpty() || chars.pop() != '[')) {
						throw new RuntimeException("Unbalanced square brackets []: " + var);
					}
				} else {
					if (chars.isEmpty()) {
						++type;
					}
					chars.push(c);
				}
			}
		}

		if (useQuote) {
			throw new RuntimeException("Unbalanced quotation: " + var);
		} else if (!chars.isEmpty()) {
			throw new RuntimeException("Unbalanced brackets: " + var);
		} else {
			if (type == 0 && !var.isEmpty()) {
				type = 1;
			}
			return type;
		}
	}

	public static TypeParser parser(String name, String value) {
		value = value.trim();
		String var3;
		boolean var4;

		if (value.startsWith("{")) {
			value = value.substring(1, value.length() - 1);

			CompoundParser cmpParser;
			for (cmpParser = new CompoundParser(name); value.length() > 0; value = value.substring(var3.length() + 1)) {
				var3 = b(value, true);
				if (var3.length() > 0) {
					var4 = false;
					cmpParser.parsers.add(a(var3, var4));
				}

				if (value.length() < var3.length() + 1) {
					break;
				}

				char c = value.charAt(var3.length());
				if (c != 44 && c != 123 && c != 125 && c != 91 && c != 93) {
					throw new RuntimeException("Unexpected token \'" + c + "\' at: " + value.substring(var3.length()));
				}
			}

			return cmpParser;
		} else if (value.startsWith("[") && !b.matcher(value).matches()) {
			value = value.substring(1, value.length() - 1);

			ListParser var2;
			for (var2 = new ListParser(name); value.length() > 0; value = value.substring(var3.length() + 1)) {
				var3 = b(value, false);
				if (var3.length() > 0) {
					var4 = true;
					var2.parsers.add(a(var3, var4));
				}

				if (value.length() < var3.length() + 1) {
					break;
				}

				char c = value.charAt(var3.length());
				if (c != 44 && c != 123 && c != 125 && c != 91 && c != 93) {
					throw new RuntimeException("Unexpected token \'" + c + "\' at: " + value.substring(var3.length()));
				}
			}

			return var2;
		} else {
			return new PrimitiveParser(name, value);
		}
	}

	private static TypeParser a(String var, boolean flag) throws RuntimeException {
		String name = parseString(var, flag);
		String value = d(var, flag);
		return parser(name, value);
	}

	private static String b(String var, boolean var1) throws RuntimeException {
		int var2 = a(var, (char) ':');
		int var3 = a(var, (char) ',');
		if (var1) {
			if (var2 == -1) {
				throw new RuntimeException("Unable to locate name/value separator for string: " + var);
			}

			if (var3 != -1 && var3 < var2) {
				throw new RuntimeException("Name error at: " + var);
			}
		} else if (var2 == -1 || var2 > var3) {
			var2 = -1;
		}

		return a(var, var2);
	}

	private static String a(String value, int pos) throws RuntimeException {
		LinkedList<Character> chars = new LinkedList<Character>();
		int currentPos = pos + 1;
		boolean flagQuote = false;
		boolean var5 = false;
		boolean var6 = false;

		for (int i = 0; currentPos < value.length(); ++currentPos) {
			char c = value.charAt(currentPos);
			if (c == '"') {
				if (b(value, currentPos)) {
					if (!flagQuote) {
						throw new RuntimeException("Illegal use of \\\": " + value);
					}
				} else {
					flagQuote = !flagQuote;
					if (flagQuote && !var6) {
						var5 = true;
					}

					if (!flagQuote) {
						i = currentPos;
					}
				}
			} else if (!flagQuote) {
				if (c != '{' && c != '[') {
					if (c == '}' && (chars.isEmpty() || chars.pop() != 123)) {
						throw new RuntimeException("Unbalanced curly brackets {}: " + value);
					}

					if (c == ']' && (chars.isEmpty() || chars.pop() != 91)) {
						throw new RuntimeException("Unbalanced square brackets []: " + value);
					}

					if (c == ',' && chars.isEmpty()) {
						return value.substring(0, currentPos);
					}
				} else {
					chars.push(c);
				}
			}

			if (!Character.isWhitespace(c)) {
				if (!flagQuote && var5 && i != currentPos) {
					return value.substring(0, i + 1);
				}

				var6 = true;
			}
		}

		return value.substring(0, currentPos);
	}

	private static String parseString(String value, boolean flag) throws RuntimeException {
		if (flag) {
			value = value.trim();
			if (value.startsWith("{") || value.startsWith("[")) {
				return "";
			}
		}

		int var2 = a(value, ':');
		if (var2 == -1) {
			if (flag) {
				return "";
			} else {
				throw new RuntimeException("Unable to locate name/value separator for string: " + value);
			}
		} else {
			return value.substring(0, var2).trim();
		}
	}

	private static String d(String var, boolean var1) throws RuntimeException {
		if (var1) {
			var = var.trim();
			if (var.startsWith("{") || var.startsWith("[")) {
				return var;
			}
		}

		int var2 = a(var, ':');
		if (var2 == -1) {
			if (var1) {
				return var;
			} else {
				throw new RuntimeException("Unable to locate name/value separator for string: " + var);
			}
		} else {
			return var.substring(var2 + 1).trim();
		}
	}

	private static int a(String var, char var1) {
		int result = 0;

		for (boolean useQuote = true; result < var.length(); ++result) {
			char c = var.charAt(result);
			if (c == '"') {
				if (!b(var, result)) {
					useQuote = !useQuote;
				}
			} else if (useQuote) {
				if (c == var1) {
					return result;
				}
				if (c == '{' || c == '[') {
					return -1;
				}
			}
		}

		return -1;
	}

	private static boolean b(String value, int pos) {
		return pos > 0 && value.charAt(pos - 1) == '\\' && !b(value, pos - 1);
	}

	static class PrimitiveParser extends TypeParser {

		private static final Pattern patDouble = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[d|D]");
		private static final Pattern patFloat = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[f|F]");
		private static final Pattern patByte = Pattern.compile("[-+]?[0-9]+[b|B]");
		private static final Pattern patLong = Pattern.compile("[-+]?[0-9]+[l|L]");
		private static final Pattern patShort = Pattern.compile("[-+]?[0-9]+[s|S]");
		private static final Pattern patInt = Pattern.compile("[-+]?[0-9]+[i|I]");
		private static final Pattern patIntDef = Pattern.compile("[-+]?[0-9]+");
		private static final Pattern patDoubleDef = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
		private static final Splitter splitter = Splitter.on(',').omitEmptyStrings();
		protected String value;

		public PrimitiveParser(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/* returns java value */
		public Object parse() throws RuntimeException {
			if (patDouble.matcher(value).matches()) {
				return parseDouble(value.substring(0, value.length() - 1));
			}
			if (patFloat.matcher(value).matches()) {
				return parseFloat(value.substring(0, value.length() - 1));
			}
			if (patByte.matcher(value).matches()) {
				return parseByte(value.substring(0, value.length() - 1));
			}
			if (patLong.matcher(value).matches()) {
				return parseLong(value.substring(0, value.length() - 1));
			}
			if (patShort.matcher(value).matches()) {
				return parseShort(value.substring(0, value.length() - 1));
			}
			if (patInt.matcher(value).matches()) {
				return parseInt(value.substring(0, value.length() - 1));
			}
			if (patIntDef.matcher(value).matches()) {
				return parseInt(value);
			}
			if (patDoubleDef.matcher(value).matches()) {
				return parseDouble(value);
			}
			if (value.equalsIgnoreCase("true")) {
				return (byte) 1;
			}
			if (value.equalsIgnoreCase("false")) {
				return (byte) 0;
			}
			if (this.value.startsWith("[") && (this.value.endsWith("]i") || this.value.endsWith("]I"))) {
				String token = value.substring(1, this.value.length() - 2);
				List<Integer> tempResult = new ArrayList<>();
				for (String s : splitter.split(token)) {
					tempResult.add(parseInt(s.trim()));
				}
				int[] result = new int[tempResult.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = tempResult.get(i);
				}
				return NBTManager.getInstance().getTagOfValue(result);
			}
			if (this.value.startsWith("[") && (this.value.endsWith("]b") || this.value.endsWith("]B"))) {
				String token = value.substring(1, this.value.length() - 2);
				List<Byte> tempResult = new ArrayList<>();
				for (String s : splitter.split(token)) {
					tempResult.add(parseByte(s.trim()));
				}
				byte[] result = new byte[tempResult.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = tempResult.get(i);
				}
				return result;
			}
			if (this.value.startsWith("[") && (this.value.endsWith("]l") || this.value.endsWith("]L"))) {
				String token = value.substring(1, this.value.length() - 2);
				List<Long> tempResult = new ArrayList<>();
				for (String s : splitter.split(token)) {
					tempResult.add(parseLong(s.trim()));
				}
				long[] result = new long[tempResult.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = tempResult.get(i);
				}
				return result;
			}
			if (value.startsWith("[") && value.endsWith("]")) {
				String token = this.value.substring(1, this.value.length() - 1);
				String[] tokens = Iterables.toArray(splitter.split(token), String.class);
				int[] result = new int[tokens.length];
				for (int i = 0; i < tokens.length; ++i) {
					result[i] = parseInt(tokens[i].trim());
				}
				return result;
			}
			if (value.startsWith("\"") && value.endsWith("\"")) {
				return value.substring(1, value.length() - 1);
			}

			value = value.replaceAll("\\\\\"", "\"");
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < value.length(); ++i) {
				if (i < value.length() - 1 && value.charAt(i) == '\\' && value.charAt(i + 1) == '\\') {
					builder.append('\\');
					++i;
				} else {
					builder.append(value.charAt(i));
				}
			}
			return builder.toString();
		}

	}

	static class ListParser extends TypeParser {

		protected List<TypeParser> parsers = new ArrayList<>();

		public ListParser(String name) {
			this.name = name;
		}

		public NBTList parse() throws RuntimeException {
			return parsers.stream().map(TypeParser::parse).collect(toCollection(NBTList::new));
		}

	}

	static class CompoundParser extends TypeParser {

		protected List<TypeParser> parsers = new ArrayList<>();

		public CompoundParser(String name) {
			this.name = name;
		}

		public NBTCompound parse() throws RuntimeException {
			NBTCompound result = new NBTCompound();
			for (TypeParser parser : this.parsers) {
				String key = parser.name;
				if (key.startsWith("\"") && key.endsWith("\"")) {
					key = StringParser.parse(key.substring(1, key.length() - 1));
				}
				result.put(key, parser.parse());
			}
			return result;
		}

	}

	public static abstract class TypeParser {

		protected String name;

		public abstract Object parse();

	}

}