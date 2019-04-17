package rainpoetry.java.common.tools;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rainpoetry.java.draw.errors.CommonException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * Get the Context ClassLoader on this thread or, if not present, the ClassLoader that
	 * loaded Kafka.
	 *
	 * This should be used whenever passing a ClassLoader to Class.forName
	 */
	public static ClassLoader getContextOrClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null)
			return getClassLoader();
		else
			return cl;
	}

	/**
	 * Get the ClassLoader which loaded Kafka.
	 */
	public static ClassLoader getClassLoader() {
		return Utils.class.getClassLoader();
	}

	/**
	 * Create a string representation of a list joined by the given separator
	 * @param list The list of items
	 * @param separator The separator
	 * @return The string representation.
	 */
	public static <T> String join(Collection<T> list,String separator) {
		Objects.requireNonNull(list);
		Iterator<T> iterator = list.iterator();
		StringBuilder sb = new StringBuilder();
		while(iterator.hasNext()) {
			sb.append(iterator.next());
			if(iterator.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	/**
	 * Instantiate the class
	 */
	public static <T> T newInstance(Class<T> c) {
		if (c == null)
			throw new CommonException("class cannot be null");
		try {
			return c.getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			throw new CommonException("Could not find a public no-argument constructor for " + c.getName(), e);
		} catch (ReflectiveOperationException | RuntimeException e) {
			throw new CommonException("Could not instantiate class " + c.getName(), e);
		}
	}

	/**
	 * Turn the given UTF8 byte array into a string
	 *
	 * @param bytes The byte array
	 * @return The string
	 */
	public static String utf8(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Read a UTF8 string from a byte buffer. Note that the position of the byte buffer is not affected
	 * by this method.
	 *
	 * @param buffer The buffer to read from
	 * @param length The length of the string in bytes
	 * @return The UTF8 string
	 */
	public static String utf8(ByteBuffer buffer, int length) {
		return utf8(buffer, 0, length);
	}

	/**
	 * Read a UTF8 string from the current position till the end of a byte buffer. The position of the byte buffer is
	 * not affected by this method.
	 *
	 * @param buffer The buffer to read from
	 * @return The UTF8 string
	 */
	public static String utf8(ByteBuffer buffer) {
		return utf8(buffer, buffer.remaining());
	}

	/**
	 * Read a UTF8 string from a byte buffer at a given offset. Note that the position of the byte buffer
	 * is not affected by this method.
	 *
	 * @param buffer The buffer to read from
	 * @param offset The offset relative to the current position in the buffer
	 * @param length The length of the string in bytes
	 * @return The UTF8 string
	 */
	public static String utf8(ByteBuffer buffer, int offset, int length) {
		if (buffer.hasArray())
			return new String(buffer.array(), buffer.arrayOffset() + buffer.position() + offset, length, StandardCharsets.UTF_8);
		else
			return utf8(toArray(buffer, offset, length));
	}

	/**
	 * Turn a string into a utf8 byte[]
	 *
	 * @param string The string
	 * @return The byte[]
	 */
	public static byte[] utf8(String string) {
		return string.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Read a byte array from the given offset and size in the buffer
	 * @param buffer The buffer to read from
	 * @param offset The offset relative to the current position of the buffer
	 * @param size The number of bytes to read into the array
	 */
	public static byte[] toArray(ByteBuffer buffer, int offset, int size) {
		byte[] dest = new byte[size];
		if (buffer.hasArray()) {
			System.arraycopy(buffer.array(), buffer.position() + buffer.arrayOffset() + offset, dest, 0, size);
		} else {
			int pos = buffer.position();
			buffer.position(pos + offset);
			buffer.get(dest);
			buffer.position(pos);
		}
		return dest;
	}

	/**
	 * Get the length for UTF8-encoding a string without encoding it first
	 *
	 * @param s The string to calculate the length for
	 * @return The length when serialized
	 */
	public static int utf8Length(CharSequence s) {
		int count = 0;
		for (int i = 0, len = s.length(); i < len; i++) {
			char ch = s.charAt(i);
			if (ch <= 0x7F) {
				count++;
			} else if (ch <= 0x7FF) {
				count += 2;
			} else if (Character.isHighSurrogate(ch)) {
				count += 4;
				++i;
			} else {
				count += 3;
			}
		}
		return count;
	}

	// 判断是否为本地地址
	public static boolean isLocal(String name) throws UnknownHostException {
		if (name.equals("localhost") || name.equals("127.0.0.1")
				|| InetAddress.getLocalHost().getHostAddress().equals(name))
			return true;
		return false;
	}

	public static void atomicMoveWithFallback(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException outer) {
			try {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
				log.debug("Non-atomic move of {} to {} succeeded after atomic move failed due to {}", source, target,
						outer.getMessage());
			} catch (IOException inner) {
				inner.addSuppressed(outer);
				throw inner;
			}
		}
	}

}
