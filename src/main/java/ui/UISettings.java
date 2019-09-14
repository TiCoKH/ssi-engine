package ui;

import static ui.UISettings.PropertyName.METHOD;
import static ui.UISettings.PropertyName.TEXT_SPEED;
import static ui.UISettings.PropertyName.ZOOM;
import static ui.UISettings.ScaleMethod.NONE;
import static ui.UISettings.TextSpeed.INSTANT;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;

public class UISettings {
	private int zoom;
	private ScaleMethod method;
	private TextSpeed textSpeed;

	private Set<PropertyChangeListener> listeners = new HashSet<>();

	public UISettings() {
		zoom = 4;
		method = NONE;
		textSpeed = INSTANT;
	}

	public void readFrom(@Nonnull FileChannel c) throws IOException {
		InputStream in = Channels.newInputStream(c);
		try {
			Properties p = new Properties();
			p.load(in);
			try {
				zoom = Integer.parseUnsignedInt(p.getOrDefault(ZOOM.name(), "4").toString());
			} catch (NumberFormatException e) {
				zoom = 4;
			}
			try {
				method = ScaleMethod.valueOf(p.getOrDefault(METHOD.name(), NONE.name()).toString());
			} catch (IllegalArgumentException e) {
				method = NONE;
			}
			try {
				textSpeed = TextSpeed.valueOf(p.getOrDefault(TEXT_SPEED.name(), INSTANT.name()).toString());
			} catch (IllegalArgumentException e) {
				textSpeed = INSTANT;
			}
		} finally {
			in.close();
		}
	}

	public void writeTo(@Nonnull FileChannel c) throws IOException {
		Writer w = Channels.newWriter(c, "ISO-8859-1");
		try {
			w.write(String.format("%s = %d%n", ZOOM.name(), getZoom()));
			w.write(String.format("%s = %s%n", METHOD.name(), getMethod().name()));
			w.write(String.format("%s = %s%n", TEXT_SPEED.name(), getTextSpeed().name()));
			w.flush();
		} finally {
			w.close();
		}
	}

	@Nonnull
	public ScaleMethod getMethod() {
		return method;
	}

	public void setMethod(@Nonnull ScaleMethod method) {
		ScaleMethod old = getMethod();
		this.method = method;
		firePropertyChange(PropertyName.METHOD, old, method);
	}

	public TextSpeed getTextSpeed() {
		return textSpeed;
	}

	public void setTextSpeed(TextSpeed textSpeed) {
		this.textSpeed = textSpeed;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		int old = getZoom();
		this.zoom = zoom;
		firePropertyChange(PropertyName.ZOOM, old, zoom);
	}

	public int zoom(int value) {
		return getZoom() * value;
	}

	public int zoom8(int value) {
		return getZoom() * 8 * value;
	}

	public void addPropertyChangeListener(@Nonnull PropertyChangeListener l) {
		listeners.add(l);
	}

	public void removePropertyChangeListener(@Nonnull PropertyChangeListener l) {
		listeners.remove(l);
	}

	private void firePropertyChange(@Nonnull PropertyName n, Object oldValue, Object newValue) {
		PropertyChangeEvent e = new PropertyChangeEvent(this, n.name(), oldValue, newValue);
		for (PropertyChangeListener l : listeners) {
			l.propertyChange(e);
		}
	}

	public enum PropertyName {
		ZOOM, METHOD, TEXT_SPEED;
	}

	public enum ScaleMethod {
		NONE, BILINEAR, BICUBIC, XBRZ;
	}

	public enum TextSpeed {
		SLOW, FAST, INSTANT;
	}
}
