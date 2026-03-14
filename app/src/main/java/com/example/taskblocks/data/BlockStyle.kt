package com.example.taskblocks.data

import androidx.annotation.StringRes
import com.example.taskblocks.R

enum class BlockStyle(val id: String, @StringRes val labelResId: Int) {
    DEFAULT("default", R.string.block_style_default),
    CUBE_3D("cube3d", R.string.block_style_cube_3d),
    METAL("metal", R.string.block_style_metal),
    WOOD("wood", R.string.block_style_wood);

    companion object {
        fun fromId(id: String?): BlockStyle = entries.find { it.id == id } ?: DEFAULT
    }
}
