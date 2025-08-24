/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.text.art.img;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArtImageRenderer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author vpc
 */
public class PixelNTextArtImageRenderer implements NTextArtImageRenderer {

    public static final PixelNTextArtImageRenderer HASH = new PixelNTextArtImageRenderer("hash", "Monospaced", 24, false, true, new char[]{' ', '#'}, 0, 0);
    public static final PixelNTextArtImageRenderer DOT = new PixelNTextArtImageRenderer("dot", "Monospaced", 24, false, true, new char[]{' ', '.'}, 0, 0);
    public static final PixelNTextArtImageRenderer DOLLAR = new PixelNTextArtImageRenderer("dollar", "Monospaced", 24, false, true, new char[]{' ', '$'}, 0, 0);
    public static final PixelNTextArtImageRenderer STAR = new PixelNTextArtImageRenderer("star", "Monospaced", 24, false, true, new char[]{' ', '*'}, 0, 0);
    public static final PixelNTextArtImageRenderer CIPHER = new PixelNTextArtImageRenderer("cipher", "Monospaced", 24, false, true, " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$".toCharArray(), 0, 0);
    public static final PixelNTextArtImageRenderer STANDARD = new PixelNTextArtImageRenderer("standard", "Monospaced", 24, false, true, new char[]{' ', /*'.', ':', '-', '-', '=', '+',*/ '░', '▒', '▓', '█'}, 0, 0);

    private final String fontName;
    private final int fontSize;
    private final boolean fontItalic;
    private final boolean fontBold;
    private final String[] lines;
    private final int outputWidth;
    private final int outputHeight;
    private final String name;

    public PixelNTextArtImageRenderer(String name, String fontName, int fontSize, boolean fontItalic, boolean fontBold, String[] lines, int outputWidth, int outputHeight) {
        if (NBlankable.isBlank(name)) {
            name = "noname";
        }
        this.name = name.startsWith("pixel:") ? name : ("pixel:" + name);
        this.fontName = NBlankable.firstNonBlank(NStringUtils.trim(fontName), "Monospaced");
        this.fontItalic = fontItalic;
        this.fontBold = fontBold;
        this.fontSize = fontSize <= 0 ? 24 : fontSize;
        this.lines = lines;
        this.outputWidth = outputWidth <= 0 ? -1 : outputWidth;
        this.outputHeight = outputHeight <= 0 ? -1 : outputHeight;
    }

    public PixelNTextArtImageRenderer(String name, String fontName, int fontSize, boolean fontItalic, boolean fontBold, char[] lines, int outputWidth, int outputHeight) {
        if (NBlankable.isBlank(name)) {
            name = "noname";
        }
        this.name = name.startsWith("pixel:") ? name : ("pixel:" + name);
        this.fontName = NBlankable.firstNonBlank(NStringUtils.trim(fontName), "Monospaced");
        this.fontItalic = fontItalic;
        this.fontBold = fontBold;
        this.fontSize = fontSize <= 0 ? 24 : fontSize;
        List<String> str = new ArrayList<>();
        for (char c : lines) {
            str.add(String.valueOf(c));
        }
        this.lines = str.toArray(new String[0]);
        this.outputWidth = outputWidth <= 0 ? -1 : outputWidth;
        this.outputHeight = outputHeight <= 0 ? -1 : outputHeight;
    }

    public PixelNTextArtImageRenderer(InputStream in) {
        this(NIOUtils.readString(in));
    }

    @Override
    public NTextArtImageRenderer setFontSize(int fontSize) {
        if (this.fontSize == fontSize) {
            return this;
        }
        return new PixelNTextArtImageRenderer(this.name, this.fontName, fontSize, this.fontItalic, this.fontBold, this.lines, this.outputWidth, this.outputHeight);
    }

    @Override
    public NTextArtImageRenderer setFontItalic(boolean fontItalic) {
        if (this.fontItalic == fontItalic) {
            return this;
        }
        return new PixelNTextArtImageRenderer(this.name, this.fontName, this.fontSize, fontItalic, this.fontBold, this.lines, this.outputWidth, this.outputHeight);
    }

    @Override
    public NTextArtImageRenderer setFontBold(boolean fontBold) {
        if (this.fontBold == fontBold) {
            return this;
        }
        return new PixelNTextArtImageRenderer(this.name, this.fontName, this.fontSize, this.fontItalic, fontBold, this.lines, this.outputWidth, this.outputHeight);
    }

    @Override
    public NTextArtImageRenderer setFontName(String fontName) {
        fontName = NBlankable.firstNonBlank(NStringUtils.trim(fontName), "Monospaced");
        if (Objects.equals(this.fontName, fontName)) {
            return this;
        }
        return new PixelNTextArtImageRenderer(this.name, fontName, this.fontSize, this.fontItalic, this.fontBold, this.lines, this.outputWidth, this.outputHeight);
    }

    @Override
    public NTextArtImageRenderer setOutputSize(int columns, int rows) {
        columns = columns <= 0 ? -1 : columns;
        columns = columns <= 0 ? -1 : columns;

        if (this.outputWidth == columns && this.outputHeight == rows) {
            return this;
        }
        return new PixelNTextArtImageRenderer(this.name, fontName, this.fontSize, this.fontItalic, this.fontBold, this.lines, columns, rows);
    }

    @Override
    public NTextArtImageRenderer setOutputColumns(int columns) {
        return setOutputSize(columns, -1);
    }

    public static NOptional<PixelNTextArtImageRenderer> ofName(String name) {
        if (NBlankable.isBlank(name)) {
            return NOptional.of(CIPHER);
        }
        if (name.startsWith("pixel:")) {
            name = name.substring("pixel:".length());
        }
        name = name.trim().toLowerCase();
        switch (name) {
            case "cipher":
                return NOptional.of(CIPHER);
            case "hash":
                return NOptional.of(HASH);
            case "standard":
                return NOptional.of(STANDARD);
            case "star":
                return NOptional.of(STAR);
            case "dot":
                return NOptional.of(DOT);
            case "dollar":
                return NOptional.of(DOLLAR);
            default: {
                try {
                    URL u = Thread.currentThread().getContextClassLoader().getResource("META-INF/textart/" + name + ".pxl");
                    if (u != null) {
                        return NOptional.of(new PixelNTextArtImageRenderer(NPath.of(u).readString()));
                    }
                } catch (Exception ex) {
                    return NOptional.ofNamedEmpty(NMsg.ofC("unable to load pixel renderer %s : %s", name, ex));
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("pixel renderer %s not found", name));
    }

    @Override
    public String getName() {
        return name;
    }

    public static boolean acceptContent(String content) {
        if (content != null && content.startsWith("npixel{")) {
            return true;
        }
        return false;
    }

    public PixelNTextArtImageRenderer(String content) {
        final NElement o = NElementParser.ofTson().parse(content);
        if (!o.isNamedObject("npixel")) {
            throw new IllegalArgumentException("invalid format");
        }
        String fontName = null;
        boolean fontItalic = false;
        boolean fontBold = false;
        int fontSize = 24;
        int columns = 0;
        int rows = 0;
        String name = null;
        List<String> styleLevel = new ArrayList<>();
        for (NElement e : o.asObject().get().children()) {
            if (e.isNamedPair()) {
                NPairElement p = e.asPair().get();
                switch (p.key().asStringValue().get()) {
                    case "fontName":
                        fontName = p.asStringValue().get();
                        break;
                    case "italic":
                        fontItalic = p.asBooleanValue().get();
                        break;
                    case "bold":
                        fontBold = p.asBooleanValue().get();
                        break;
                    case "name":
                        name = p.asStringValue().get();
                        break;
                    case "fontSize":
                        fontSize = p.asIntValue().get();
                        break;
                    case "columns":
                        columns = p.asIntValue().get();
                        break;
                    case "rows":
                        rows = p.asIntValue().get();
                        break;
                    case "pattern": {
                        if (p.isArray()) {
                            for (NElement ne : p.asArray().get().children()) {
                                final String c = ne.asStringValue().get();
                                if (c.isEmpty()) {
                                    throw new IllegalArgumentException("invalid format");
                                }
                                if (styleLevel.isEmpty() && !c.equals(" ")) {
                                    styleLevel.add(" ");
                                }
                                styleLevel.add(c);
                            }
                        } else {
                            for (char c : p.asStringValue().get().toCharArray()) {
                                if (styleLevel.isEmpty() && c != ' ') {
                                    styleLevel.add(" ");
                                }
                                styleLevel.add(String.valueOf(c));
                            }
                        }
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("invalid format");
                }
            } else {
                throw new IllegalArgumentException("invalid format");
            }
        }
        if (styleLevel.isEmpty()) {
            switch (NStringUtils.trim(name).toLowerCase()) {
                case "block": {
                    for (char c : " █".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                case "star": {
                    for (char c : " *".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                case "dot": {
                    for (char c : " .".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                case "hash": {
                    for (char c : " #".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                case "simple": {
                    for (char c : " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                case "unicode": {
                    for (char c : " .:--=+░▒▓█".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
                default: {
                    for (char c : " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$".toCharArray()) {
                        styleLevel.add(String.valueOf(c));
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(name)) {
            name = "noname";
        }
        this.name = name.startsWith("pixel:") ? name : ("pixel:" + name);
        this.fontName = NBlankable.firstNonBlank(NStringUtils.trim(fontName), "Monospaced");
        this.fontSize = fontSize <= 0 ? 24 : fontSize;
        this.fontItalic = fontItalic;
        this.fontBold = fontBold;
        this.lines = styleLevel.toArray(new String[0]);
        this.outputWidth = columns <= 0 ? -1 : columns;
        this.outputHeight = rows <= 0 ? -1 : rows;
    }

    public NText render(NText text) {
        final String filteredText = text.filteredText();
        int fontSize = this.fontSize;
        if (fontSize <= 0) {
            fontSize = 24;
        }
        String fontName = this.fontName;
        BufferedImage tmpToCalculateTextSize = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tmpToCalculateTextSize.createGraphics();
        Font font = new Font(fontName,
                (this.fontBold ? Font.BOLD : Font.PLAIN) | (this.fontItalic ? Font.ITALIC : Font.PLAIN),
                fontSize
        );
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int width = fm.stringWidth(filteredText) + 2;
        int height = fm.getHeight() + 2;
        g2.dispose();
        // Create actual image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2 = img.createGraphics();
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
        g2.drawString(filteredText, 1, fm.getAscent());
        g2.dispose();
        return render(img, d -> {
            if (outputWidth <= 0 && outputHeight <= 0) {
                return new Dimension(80, (int) (1.0 * d.height * 80 / d.width));
            }
            if (outputWidth <= 0) {
                return new Dimension((int) (1.0 * d.width * outputHeight / d.height), outputHeight);
            }
            if (outputHeight <= 0) {
                return new Dimension(outputWidth, (int) (1.0 * d.height * outputWidth / d.width));
            }
            return new Dimension(outputWidth, outputHeight);
        });
    }

    @Override
    public NText render(Image img) {
        return render(toBufferedImage(img), d -> {
            if (outputWidth <= 0 && outputHeight <= 0) {
                return new Dimension(80, d.height / d.width * 80);
            }
            if (outputWidth <= 0) {
                return new Dimension(d.width / d.height * outputHeight, outputHeight);
            }
            if (outputHeight <= 0) {
                return new Dimension(outputWidth, d.height / d.width * outputWidth);
            }
            return new Dimension(outputWidth, outputHeight);
        });
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Ensure all pixels are loaded
        img = new ImageIcon(img).getImage();

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    public BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        // Improve quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }

    public NText render(Image img, int width, int height) {
        return render(toBufferedImage(img), width, height);
    }

    private NText render(BufferedImage img, Function<Dimension, Dimension> dim) {
        Dimension d2 = dim.apply(new Dimension(img.getWidth(), img.getHeight()));
        try {
            ImageIO.write(img, "png", new File("/home/vpc/rendered-image-1.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (d2.width != img.getWidth() || d2.height != img.getHeight()) {
            img = resize(img, d2.width, d2.height);
        }
        try {
            ImageIO.write(img, "png", new File("/home/vpc/rendered-image-2.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int width = img.getWidth();
        int height = img.getHeight();
        // Convert pixels to ASCII
        StringBuilder sb = new StringBuilder();
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int y = 0; y < height; y += 1) {
            for (int x = 0; x < width; x += 1) {
                int rgb = img.getRGB(x, y);
                int gray = ((rgb >> 16) & 0xff + (rgb >> 8) & 0xff + (rgb & 0xff)) / 3;
                min = Math.min(gray, min);
                max = Math.max(gray, max);
            }
        }

        for (int y = 0; y < height; y += 1) {
            for (int x = 0; x < width; x += 1) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int gray = 255 - (int) (0.299 * r + 0.587 * g + 0.114 * b);
                min = Math.min(gray, min);
                max = Math.max(gray, max);
                double grayPercent = 1.0 * gray / max;
                double grayPercentAbsolute = 1.0 * gray / 255.0;
                NText ch = charFor(rgb, gray, grayPercent, grayPercentAbsolute, min, max);
                sb.append(ch);
            }
            sb.append('\n');
        }
        return NText.ofPlain(sb.toString());
    }

    private NText charFor(int rgb, int gray, double grayPercent, double grayPercentAbsolute, int minGray, int maxGray) {
        int idx = (int) Math.round(grayPercent * (lines.length - 1));
        return NText.ofPlain(String.valueOf(lines[idx].charAt(0)));
    }

}
