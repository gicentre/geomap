

# GeoMap #

## Constructor methods ##

Each `GeoMap` has two constructor methods.

<a>
<pre><code>GeoMap(xOrigin, yOrigin, mapWidth, mapHeight, parent)</code></pre>
</a>

The first constructor method creates a map with the given bounds. It has five parameters:

  1. `xOrigin` is the position of the map from the left of the sketch. It should be a number.
  1. `yOrigin` is the position of the map from the top of the sketch. It should be a number.
  1. `mapWidth` is the width of the map. It should be a number.
  1. `mapHeight` is the height of the map. It should be a number.
  1. `parent` is the parent sketch. It should be a `PApplet`. In most cases, simply use `this` to refer to the current Processing sketch.


---

<a>
<pre><code>GeoMap(PApplet parent)</code></pre>
</a>

The second constructor method creates a map with the default bounds. It has one parameter:

  1. `parent` is the parent sketch. It should be a `PApplet`. In most cases, simply use `this` to refer to the current Processing sketch.

## Read methods ##

Each `GeoMap` has one method to read data from an [ESRI shapefile](http://en.wikipedia.org/wiki/Shapefile). Note that a shapefile consists of three files:

  * _fileName.shp_ contains the geometry.
  * _fileName.dbf_ contains the attributes.
  * _fileName.dbx_ contains the index.

Each file should be placed in the data directory of the current Processing sketch. The easiest way to do this is to drag and drop each file into the Processing development environment.

```processing
geoMap.readFile(fileName)```

Reads data from an [ESRI shapefile](http://en.wikipedia.org/wiki/Shapefile). It has one parameter:

  1. `fileName` is the name of the file without the extension. It should be a `String`.

## Draw methods ##

Each `GeoMap` has three draw methods.

```processing
geoMap.draw()```

The first draw method draws the map in the parent sketch.


---

```processing
geoMap.draw(id)```

The second draw method draws the feature that matches the given id. It has one parameter:

  1. `id` is the id that matches the feature to draw. It should be an `int`.


---

```processing
geoMap.draw(attribute, column)```

The third draw method draws the features that match the given attribute in the given column in the map's attribute table. It has two parameters:

  1. `attribute` is the attribute that matches the features to draw. It should be a `String`. Note that even when `attribute` is a number, it should be passed as a `String`. (For example, `500` should be passed as `"500"`.)
  1. `column` is the column in the map's attribute table. It should be an `int`. Note that the first column in the map's attribute table is `0`, the second is `1`, the third is `2` and so on.

## Feature methods ##

Each `GeoMap` has one or more features. For example, in a map of the world, the features might be the countries. Each feature has an id that can be used to query the map's attribute table. Each `GeoMap` has one feature method.

```processing
geoMap.getFeatures()```

Gets the map's features. These will be in a `Set`. However, in most cases this method will be used to iterate over a map's features:

```
for (int id : geoMap.getFeatures().keySet())
{
  geoMap.draw(id);
}
```

To learn more about iterating over a map's features, see the [Draw a choropleth map](Examples#Draw_a_choropleth_map.md) example.

## Attribute methods ##

Each `GeoMap` has one or more attributes stored in a `Table`. The first column in the map's attribute table (column `0`) stores an id for each feature in the map. Additional columns store attributes read from an [ESRI shapefile](http://en.wikipedia.org/wiki/Shapefile) by the `geoMap.readFile()` method. Each `GeoMap` has two attribute methods.

```processing
geoMap.getAttributes()```

The first attribute method gets the map's attributes. These will be in a `Table`.

```processing
geoMap.getID(sketchX, sketchY)```

The second attribute method gets the id of the feature at the given location in the sketch or `-1` in the event that there is no feature. It will be an `int`. It has two parameters:

  1. `sketchX` is the position from the left of the sketch. It should be a number.
  1. `sketchY` is the position from the top of the sketch. It should be a number.

# Table #

Each `GeoMap` has one or more attributes stored in a `Table`. The first column in the map's attribute table (column `0`) stores an id for each feature in the map. Additional columns store attributes read from an [ESRI shapefile](http://en.wikipedia.org/wiki/Shapefile) by the `geoMap.readFile()` method.

## Write methods ##

Each `Table` has one write method.

```processing
table.writeAsTable(maxNumRows)```

Writes the map's attribute table to the console in the Processing development environment. It has one parameter:

  1. `maxNumRows` is the maximum number of rows to write to the console in the Processing development environment. It should be an `int`.

## Query methods ##

Each `Table` has three query methods.

```processing"
table.getString(id, column)```

Looks for the feature in the `Table` with the given id, then gets the corresponding attribute in the given column as a `String`. It has two parameters:

  1. `id` is the id that matches the feature. It should be a `String`. Note that even when `id` is a number, it should be passed as a `String`. (For example, `500` should be passed as `"500"`.) Use `Integer.toString(id)` to covert `id` from an `int` to a `String`.
  1. `column` is the number of the column. It should be an `int`.


---


```processing"
table.getInt(id, column)```

Looks for the feature in the `Table` with the given id, then gets the corresponding attribute in the given column as an `int`. It has two parameters:

  1. `id` is the id that matches the feature. It should be a `String`. Note that even when `id` is a number, it should be passed as a `String`. (For example, `500` should be passed as `"500"`.) Use `Integer.toString(id)` to covert `id` from an `int` to a `String`.
  1. `column` is the number of the column. It should be an `int`.


---


```processing"
table.getFloat(id, column)```

Looks for the feature in the `Table` with the given id, then gets the corresponding attribute in the given column as a `float`. It has two parameters:

  1. `id` is the id that matches the feature. It should be a `String`. Note that even when `id` is a number, it should be passed as a `String`. (For example, `500` should be passed as `"500"`.) Use `Integer.toString(id)` to covert `id` from an `int` to a `String`.
  1. `column` is the number of the column. It should be an `int`.