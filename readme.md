### layout.xml
```xml
    <com.frisky.utils.ColorSeekBar
        android:id="@+id/color_seekbar"
        android:layout_width="match_parent"
        android:layout_height="52dp"
        android:paddingLeft="30dp"
        android:paddingRight="30dp"
        app:csb_barHeight="10dp"
        app:csb_colorSeeds="@array/test_seeds"
        app:csb_cornerRadius="5dp"
        app:csb_thumbBorder="1dp"
        app:csb_thumbBorderColor="#ff0000"
        app:csb_thumbRadius="10dp"
        app:csb_thumbSelectedRadius="20dp"
        app:csb_value="#0f0" />
```

### attr.xml
```xml
    <array name="test_seeds">
        <item>@color/purple_200</item>
        <item>@color/purple_700</item>
        <item>@color/teal_200</item>
        <item>@color/white</item>
        <item>@color/black</item>
    </array>
```

### support method

| Method                              | Description |
| ----------------------------------- | ----------- |
| setListener / getListener           |             |
| setSelectedColor / getSelectedColor |             |
| setColorSeeds  /getColorSeeds       |             |



![sample](app\sample.png)