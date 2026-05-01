package org.bestraxstudio.playerrooms.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static Component updateString(String text) {
        TextComponent.Builder componentBuilder = Component.text();
        Matcher matcher = HEX_PATTERN.matcher(text);

        int lastIndex = 0;
        TextColor currentColor = null;

        while (matcher.find()) {
            String colorCode = matcher.group();
            int startIndex = matcher.start();

            if (lastIndex < startIndex) {
                String part = text.substring(lastIndex, startIndex);
                componentBuilder.append(Component.text(part).color(currentColor));
            }

            currentColor = TextColor.fromHexString(colorCode);
            lastIndex = matcher.end();
        }

        if (lastIndex < text.length()) {
            String remainingPart = text.substring(lastIndex);
            componentBuilder.append(Component.text(remainingPart).color(currentColor));
        }

        return componentBuilder.build();
    }
}