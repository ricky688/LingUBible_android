package com.lingubible.app.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedCardShapeTest {
    private fun assertCorners(index: Int, count: Int, outer: Int, top: Int, bottom: Int) {
        val shape = getGroupedItemShape(index, count, outerRadius = outer.dp)
        val size = Size(400f, 200f)
        val density = Density(2f)
        assertEquals(top * 2f, shape.topStart.toPx(size, density), 0f)
        assertEquals(top * 2f, shape.topEnd.toPx(size, density), 0f)
        assertEquals(bottom * 2f, shape.bottomStart.toPx(size, density), 0f)
        assertEquals(bottom * 2f, shape.bottomEnd.toPx(size, density), 0f)
    }

    @Test fun singleCardKeepsAllOuterCorners() = assertCorners(0, 1, 28, 28, 28)

    @Test fun twoCardsHaveSmallFacingCorners() {
        assertCorners(0, 2, 20, 20, 4)
        assertCorners(1, 2, 20, 4, 20)
    }

    @Test fun middleCardsHaveOnlySmallCorners() {
        assertCorners(0, 3, 28, 28, 4)
        assertCorners(1, 3, 28, 4, 4)
        assertCorners(2, 3, 28, 4, 28)
    }

    @Test fun pressRadiusDoesNotRoundTheSharedEdges() {
        assertCorners(0, 3, 16, 16, 4)
        assertCorners(1, 3, 16, 4, 4)
        assertCorners(2, 3, 16, 4, 16)
    }
}
