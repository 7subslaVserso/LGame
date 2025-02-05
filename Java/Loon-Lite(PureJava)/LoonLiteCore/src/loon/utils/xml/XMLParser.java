/**
 * Copyright 2008 - 2011
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.1
 */
package loon.utils.xml;

import java.util.Iterator;

import loon.BaseIO;
import loon.Json;
import loon.LSysException;
import loon.LSystem;
import loon.utils.StrBuilder;
import loon.utils.StringUtils;
import loon.utils.TArray;

/**
 * 自带的XML解析用类
 */
public class XMLParser {

	public final static String jsonToXml(String data) {
		if (StringUtils.isEmpty(data)) {
			return LSystem.EMPTY;
		}
		Object jsonData = BaseIO.loadJsonObjectContext(data);
		return jsonToXml(null, jsonData);
	}

	public final static String jsonToXml(String name, Object data) {
		String nameValue = (name == null) ? "" : name;
		if (data instanceof Json.Array) {
			StrBuilder builder = new StrBuilder("<" + nameValue + ">");
			builder.append(LSystem.LS);
			Json.Array arrays = (Json.Array) data;
			for (int i = 0; i < arrays.length(); i++) {
				builder.append(jsonToXml(null, arrays.getObject(i)));
			}
			builder.append("</" + nameValue + ">");
			builder.append(LSystem.LS);
			return builder.toString();
		} else if (data instanceof Json.Object) {
			StrBuilder builder = new StrBuilder(StringUtils.isEmpty(nameValue) ? "<objects>\n" : "<" + nameValue + ">");
			Json.Object objects = ((Json.Object) data);
			Json.TypedArray<String> keys = objects.keys();
			if (keys.length() > 0) {
				for (Iterator<String> it = keys.iterator(); it.hasNext();) {
					String key = it.next();
					if (objects.isArray(key)) {
						builder.append(jsonToXml(key, objects.getArray(key)));
					} else if (objects.isString(key)) {
						builder.append(jsonToXml(key, objects.getString(key)));
					} else if (objects.isBoolean(key)) {
						builder.append(jsonToXml(key, objects.getBoolean(key)));
					} else if (objects.isNumber(key)) {
						builder.append(jsonToXml(key, objects.getNumber(key)));
					} else {
						builder.append(jsonToXml(key, objects.getObject(key)));
					}
				}
				builder.append(StringUtils.isEmpty(nameValue) ? "</objects>" : "</" + nameValue + ">");

			}
			return builder.toString();
		} else if (data instanceof Object) {
			return "<" + nameValue + ">" + data + "</" + nameValue + ">" + LSystem.LS;
		} else if (data == null) {
			return "<" + nameValue.trim() + "/>" + LSystem.LS;
		} else {
			throw new LSysException("Data type " + data.getClass() + " not yet supported");
		}
	}

	public final static String jsonToTypeXml(String data) {
		if (StringUtils.isEmpty(data)) {
			return "<null/>";
		}
		Object jsonData = BaseIO.loadJsonObjectContext(data);
		return jsonToTypeXml(null, jsonData);
	}

	public final static String jsonToTypeXml(String name, Object data) {
		String nameValue = ((name == null) ? "" : " name=\"" + name + "\"");
		if (data instanceof Number) {
			return "<number" + nameValue + ">" + data + "</number>";
		} else if (data instanceof String) {
			return "<string" + nameValue + ">" + data + "</string>";
		} else if (data instanceof Boolean) {
			return "<boolean" + nameValue + ">" + data + "</boolean>";
		} else if (data instanceof Json.Array) {
			StrBuilder builder = new StrBuilder("<array" + nameValue + ">");
			builder.append(LSystem.LS);
			Json.Array arrays = (Json.Array) data;
			for (int i = 0; i < arrays.length(); i++) {
				builder.append(jsonToTypeXml(null, arrays.getObject(i)));
			}
			builder.append(LSystem.LS);
			builder.append("</array>");
			return builder.toString();
		} else if (data instanceof Json.Object) {
			StrBuilder builder = new StrBuilder("<objects" + nameValue + ">");
			builder.append(LSystem.LS);
			Json.Object objects = ((Json.Object) data);
			Json.TypedArray<String> keys = objects.keys();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				if (objects.isArray(key)) {
					builder.append(jsonToTypeXml(key, objects.getArray(key)));
				} else if (objects.isString(key)) {
					builder.append(jsonToTypeXml(key, objects.getString(key)));
				} else if (objects.isBoolean(key)) {
					builder.append(jsonToTypeXml(key, objects.getBoolean(key)));
				} else if (objects.isNumber(key)) {
					builder.append(jsonToTypeXml(key, objects.getNumber(key)));
				} else {
					builder.append(jsonToTypeXml(key, objects.getObject(key)));
				}
			}
			builder.append("</objects>");
			return builder.toString();
		} else if (data instanceof Object) {
			return "<object" + nameValue + ">" + data + "</object>";
		} else if (data == null) {
			return "<" + nameValue.trim() + "/>";
		} else {
			throw new LSysException("Data type " + data.getClass() + " not yet supported");
		}
	}

	public static final int OPEN_TAG = 0;

	public static final int CLOSE_TAG = 1;

	public static final int OPEN_CLOSE_TAG = 2;

	private TArray<XMLElement> stack = new TArray<>();

	private XMLElement topElement;

	private XMLElement rootElement;

	private StrBuilder header = new StrBuilder(1024);

	private void pushElement(XMLElement root, int idx, XMLListener l) {
		if (this.topElement == null) {
			this.rootElement = root;
		} else {
			this.topElement.addContents(root);
		}
		this.stack.add(root);
		this.topElement = root;
		if (l != null) {
			l.addElement(idx, root);
		}
	}

	private void popElement(int idx, XMLListener l) {
		if (l != null) {
			l.endElement(idx, this.topElement);
		}
		this.stack.pop();
		if (stack.size() > 0) {
			this.topElement = this.stack.peek();
		} else {
			this.topElement = null;
		}
	}

	private void newElement(String context, XMLListener l, int index) {
		String o = LSystem.EMPTY;
		int i;
		String str1;
		if (context.endsWith("/>")) {
			i = 2;
			str1 = context.substring(1, context.length() - 2);
		} else if (context.startsWith("</")) {
			i = 1;
			str1 = context.substring(2, context.length() - 1);
		} else {
			i = 0;
			str1 = context.substring(1, context.length() - 1);
		}
		try {
			if (str1.indexOf(' ') < 0) {
				o = str1;
				switch (i) {
				case OPEN_TAG:
					pushElement(new XMLElement(o), index, l);
					break;
				case CLOSE_TAG:
					if (this.topElement.getName().equals(o)) {
						popElement(index, l);
					} else {
						throw new LSysException(
								"Expected close of '" + this.topElement.getName() + "' instead of " + context);
					}
					break;
				case OPEN_CLOSE_TAG:
					pushElement(new XMLElement(o), index, l);
					popElement(index, l);
					break;
				}
			} else {
				XMLElement el = null;
				o = str1.substring(0, str1.indexOf(' '));
				switch (i) {
				case OPEN_TAG:
					el = new XMLElement(o);
					pushElement(el, index, l);
					break;
				case CLOSE_TAG:
					throw new LSysException("Syntax Error: " + context);
				case OPEN_CLOSE_TAG:
					el = new XMLElement(o);
					pushElement(el, index, l);
					popElement(index, l);
					break;
				}
				String str2 = str1.substring(str1.indexOf(' ') + 1);
				int start = 0;
				int end = 0;

				StrBuilder sbr1 = new StrBuilder(128);
				StrBuilder sbr2 = new StrBuilder(32);
				for (int m = 0; m < str2.length(); m++) {
					switch (str2.charAt(m)) {
					case '"':
						start = start != 0 ? 0 : 1;
						break;
					case ' ':
						if ((end == 1) && (start == 1)) {
							sbr1.append(str2.charAt(m));
						} else if (sbr2.length() > 0) {
							String key = sbr2.toString();
							String value = sbr1.toString();
							if (key.length() > 0) {
								XMLAttribute a = el.addAttribute(key, value);
								a.element = el;
								if (l != null) {
									l.addAttribute(index, a);
								}
							}
							end = 0;
							sbr1.setLength(0);
							sbr2.setLength(0);
						}
						break;
					case '=':
						if (start == 0) {
							end = 1;
						}
						break;
					case '!':
					case '#':
					case '$':
					case '%':
					case '&':
					case '\'':
					case '(':
					case ')':
					case '*':
					case '+':
					case ',':
					case '-':
					case '.':
					case '/':
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
					case ':':
					case ';':
					case '<':
					default:
						if (end != 0) {
							sbr1.append(str2.charAt(m));
						} else {
							sbr2.append(str2.charAt(m));
						}
					}

				}
				if (sbr1.length() > 0) {
					String key = sbr2.toString();
					String value = sbr1.toString();
					XMLAttribute a = el.addAttribute(key, value);
					a.element = el;
					if (l != null) {
						l.addAttribute(index, a);
					}
				}
			}
		} catch (Throwable e) {
			throw new LSysException("Cannot parse element '" + context + "' - (" + e + ")", e);
		}
	}

	private void newData(String data, XMLListener l, int index) {
		if (this.topElement != null) {
			XMLData xdata = new XMLData(data);
			this.topElement.addContents(xdata);
			if (l != null) {
				l.addData(index, xdata);
			}
		} else if (this.rootElement == null) {
			this.header.append(data);
		}
	}

	private void newComment(String comment, XMLListener l, int index) {
		if (this.topElement != null) {
			XMLComment c = new XMLComment(comment.substring(4, comment.length() - 3));
			this.topElement.addContents(c);
			if (l != null) {
				l.addComment(index, c);
			}
		} else if (this.rootElement == null) {
			this.header.append(comment);
		}
	}

	private void newProcessing(String p, XMLListener l, int index) {
		if (this.topElement != null) {
			XMLProcessing xp = new XMLProcessing(p.substring(2, p.length() - 2));
			this.topElement.addContents(xp);
			if (l != null) {
				l.addHeader(index, xp);
			}
		} else if (this.rootElement == null) {
			this.header.append(p);
		}
	}

	private XMLDocument parseText(String text, XMLListener l) {
		int count = 0;
		for (XMLTokenizer tokenizer = new XMLTokenizer(text); tokenizer.hasMoreElements();) {
			String str = tokenizer.nextElement();
			if ((str.startsWith("<?")) && (str.endsWith("?>"))) {
				newProcessing(str, l, count);
			} else if ((str.startsWith("<!--")) && (str.endsWith("-->"))) {
				newComment(str, l, count);
			} else if (str.charAt(0) == '<') {
				newElement(str, l, count);
			} else {
				newData(str, l, count);
			}
			count++;
		}

		return new XMLDocument(this.header.toString(), this.rootElement);
	}

	public static XMLDocument parse(String file) {
		return parse(file, null);
	}

	public static XMLDocument parse(String file, XMLListener l) {
		String context = BaseIO.loadText(file);
		if (StringUtils.isEmpty(context)) {
			throw new LSysException("The file [" + file + "] is null !");
		}
		return new XMLParser().parseText(BaseIO.loadText(file), l);
	}

	public static XMLDocument loadText(String context) {
		return loadText(context, null);
	}

	public static XMLDocument loadText(String context, XMLListener l) {
		if (StringUtils.isEmpty(context)) {
			throw new LSysException("The context is null !");
		}
		return new XMLParser().parseText(context, l);
	}

	public void dispose() {
		if (stack != null) {
			stack.clear();
			stack = null;
		}
		if (topElement != null) {
			topElement.dispose();
			topElement = null;
		}
		if (rootElement != null) {
			rootElement.dispose();
			rootElement = null;
		}
	}

}
