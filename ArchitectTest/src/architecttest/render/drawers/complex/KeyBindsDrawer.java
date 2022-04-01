/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architecttest.render.text.Text;
import static architecttest.render.utils.Color.LightGreen;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author AK47
 */
class KeyBindsDrawer extends Drawer {

	@Override
	protected void drawAlways() {
		try {
			Text.setCaret(0.6f, 0.9f);

			InputStream in = Text.class.getResourceAsStream("/architect/test/input/binds.properties");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			reader.lines().sequential().forEach(line -> Text.print(LightGreen, line));
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
