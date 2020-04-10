package com.itextpdf.ocr.imageformats;

import com.itextpdf.test.annotations.type.IntegrationTest;

import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ImageFormatIntegrationTestExecutable extends ImageFormatIntegrationTest {
    public ImageFormatIntegrationTestExecutable() {
        super("executable");
    }
}
