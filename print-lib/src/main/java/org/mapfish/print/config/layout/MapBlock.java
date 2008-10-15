/*
 * Copyright (C) 2008  Camptocamp
 *
 * This file is part of MapFish Server
 *
 * MapFish Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MapFish Server.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.config.layout;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.*;
import org.mapfish.print.map.MapChunkDrawer;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

public class MapBlock extends Block {
    private int spacingAfter = 0;
    private int height = 453;
    private int width = 340;
    private int absoluteX = Integer.MIN_VALUE;
    private int absoluteY = Integer.MIN_VALUE;
    private double overviewMap = Double.NaN;

    public void render(PJsonObject params, PdfElement target, RenderingContext context) throws DocumentException {
        Transformer transformer = createTransformer(context, params);

        final MapChunkDrawer drawer = new MapChunkDrawer(context.getCustomBlocks(), transformer, overviewMap, params, context, getBackgroundColorVal(context, params));

        if (isAbsolute()) {
            context.getCustomBlocks().addAbsoluteDrawer(new PDFCustomBlocks.AbsoluteDrawer() {
                public void render(PdfContentByte dc) {
                    final Rectangle rectangle = new Rectangle(absoluteX, absoluteY - height, absoluteX + width, absoluteY);
                    drawer.render(rectangle, dc);
                }
            });
        } else {
            target.add(PDFUtils.createPlaceholderTable(transformer.getPaperW(), transformer.getPaperH(), spacingAfter, drawer, align, context.getCustomBlocks()));
        }
    }

    public Transformer createTransformer(RenderingContext context, PJsonObject params) {
        Integer dpi = params.optInt("dpi");
        if (dpi == null) {
            dpi = context.getGlobalParams().getInt("dpi");
        }
        if (!context.getConfig().getDpis().contains(dpi)) {
            throw new InvalidJsonValueException(params, "dpi", dpi);
        }

        final PJsonArray center = params.getJSONArray("center");
        final PJsonObject parent = (PJsonObject) params.getParent().getParent();
        String units = parent.getString("units");
        return new Transformer(center.getFloat(0), center.getFloat(1), width, height,
                params.getInt("scale"), dpi, units, params.optFloat("rotation", 0.0F) * Math.PI / 180.0);
    }

    public void setSpacingAfter(int spacingAfter) {
        this.spacingAfter = spacingAfter;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    boolean isAbsolute() {
        return absoluteX != Integer.MIN_VALUE &&
                absoluteY != Integer.MIN_VALUE;
    }

    public void setAbsoluteX(int absoluteX) {
        this.absoluteX = absoluteX;
    }

    public void setAbsoluteY(int absoluteY) {
        this.absoluteY = absoluteY;
    }

    public MapBlock getMap() {
        return Double.isNaN(overviewMap) ? this : null;
    }

    public void printClientConfig(JSONWriter json) throws JSONException {
        json.object();
        json.key("width").value(width);
        json.key("height").value(height);
        json.endObject();
    }

    public void setOverviewMap(double overviewMap) {
        this.overviewMap = overviewMap;
    }
}
