package JSON;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Gson {
	public static void main(String[] args) {
		String str = writeJSON();
		// readJSON(str);
		readJSON_Allen(str);
	}

	public static String writeJSON() {
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer;
		try {
			// writer = new JsonWriter(new FileWriter("c:\\user.json.txt"));
			writer = new JsonWriter(stringWriter);
			writer.beginObject(); // {
			writer.name("name").value("mkyong"); // "name" : "mkyong"
			writer.name("age").value(29); // "age" : 29
			writer.name("messages"); // "messages" :
			writer.beginArray(); // [
			writer.value("msg 1"); // "msg 1"
			writer.value("msg 2"); // "msg 2"
			writer.value("msg 3"); // "msg 3"
			writer.endArray(); // ]
			writer.endObject(); // }
			System.out.println(stringWriter);
			writer.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	public static void readJSON(String str) {
		try {
			StringReader stringReader = new StringReader(str);
			// JsonReader reader = new JsonReader(new FileReader(
			// "c:\\user.json.txt"));
			JsonReader reader = new JsonReader(stringReader);
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("name")) {
					System.out.println(reader.nextString());
				} else if (name.equals("age")) {
					System.out.println(reader.nextInt());
				} else if (name.equals("message")) {
					// read array
					reader.beginArray();
					while (reader.hasNext()) {
						System.out.println(reader.nextString());
					}
					reader.endArray();
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}
			reader.endObject();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readJSON_Allen(String str) {
		try {
			StringReader stringReader = new StringReader(str);
			JsonReader reader = new JsonReader(stringReader);
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("name")) {
					System.out.println(reader.nextString());
				} else if (name.equals("age")) {
					System.out.println(reader.nextInt());
				} else if (name.equals("message")) {
					// read array
					reader.beginArray();
					while (reader.hasNext()) {
						System.out.println(reader.nextString());
					}
					reader.endArray();
				} else {
					reader.skipValue(); // avoid some unhandle events
				}
			}
			reader.endObject();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}