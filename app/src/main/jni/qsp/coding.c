/* Copyright (C) 2001-2024 Val Argunov (byte AT qsp DOT org) */
/*
* This library is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2.1 of the License, or
* (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/

#include "coding.h"
#include "text.h"
#include "variant.h"

const unsigned char qspCP1251ToKOI8RTable[] =
{
    0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
    0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
    0x9A, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0xB3, 0xBF, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
    0x9C, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x9E, 0xA3, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
    0xE1, 0xE2, 0xF7, 0xE7, 0xE4, 0xE5, 0xF6, 0xFA, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF, 0xF0,
    0xF2, 0xF3, 0xF4, 0xF5, 0xE6, 0xE8, 0xE3, 0xFE, 0xFB, 0xFD, 0xFF, 0xF9, 0xF8, 0xFC, 0xE0, 0xF1,
    0xC1, 0xC2, 0xD7, 0xC7, 0xC4, 0xC5, 0xD6, 0xDA, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF, 0xD0,
    0xD2, 0xD3, 0xD4, 0xD5, 0xC6, 0xC8, 0xC3, 0xDE, 0xDB, 0xDD, 0xDF, 0xD9, 0xD8, 0xDC, 0xC0, 0xD1
};

const int qspKOI8RToUnicodeTable[] =
{
    0x2500, 0x2502, 0x250C, 0x2510, 0x2514, 0x2518, 0x251C, 0x2524,
    0x252C, 0x2534, 0x253C, 0x2580, 0x2584, 0x2588, 0x258C, 0x2590,
    0x2591, 0x2592, 0x2593, 0x2320, 0x25A0, 0x2219, 0x221A, 0x2248,
    0x2264, 0x2265, 0x00A0, 0x2321, 0x00B0, 0x00B2, 0x00B7, 0x00F7,
    0x2550, 0x2551, 0x2552, 0x0451, 0x2553, 0x2554, 0x2555, 0x2556,
    0x2557, 0x2558, 0x2559, 0x255A, 0x255B, 0x255C, 0x255D, 0x255E,
    0x255F, 0x2560, 0x2561, 0x0401, 0x2562, 0x2563, 0x2564, 0x2565,
    0x2566, 0x2567, 0x2568, 0x2569, 0x256A, 0x256B, 0x256C, 0x00A9,
    0x044E, 0x0430, 0x0431, 0x0446, 0x0434, 0x0435, 0x0444, 0x0433,
    0x0445, 0x0438, 0x0439, 0x043A, 0x043B, 0x043C, 0x043D, 0x043E,
    0x043F, 0x044F, 0x0440, 0x0441, 0x0442, 0x0443, 0x0436, 0x0432,
    0x044C, 0x044B, 0x0437, 0x0448, 0x044D, 0x0449, 0x0447, 0x044A,
    0x042E, 0x0410, 0x0411, 0x0426, 0x0414, 0x0415, 0x0424, 0x0413,
    0x0425, 0x0418, 0x0419, 0x041A, 0x041B, 0x041C, 0x041D, 0x041E,
    0x041F, 0x042F, 0x0420, 0x0421, 0x0422, 0x0423, 0x0416, 0x0412,
    0x042C, 0x042B, 0x0417, 0x0428, 0x042D, 0x0429, 0x0427, 0x042A
};

const int qspCP1251ToUnicodeTable[] =
{
    0x0402, 0x0403, 0x201A, 0x0453, 0x201E, 0x2026, 0x2020, 0x2021,
    0x20AC, 0x2030, 0x0409, 0x2039, 0x040A, 0x040C, 0x040B, 0x040F,
    0x0452, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2013, 0x2014,
    0x0020, 0x2122, 0x0459, 0x203A, 0x045A, 0x045C, 0x045B, 0x045F,
    0x00A0, 0x040E, 0x045E, 0x0408, 0x00A4, 0x0490, 0x00A6, 0x00A7,
    0x0401, 0x00A9, 0x0404, 0x00AB, 0x00AC, 0x00AD, 0x00AE, 0x0407,
    0x00B0, 0x00B1, 0x0406, 0x0456, 0x0491, 0x00B5, 0x00B6, 0x00B7,
    0x0451, 0x2116, 0x0454, 0x00BB, 0x0458, 0x0405, 0x0455, 0x0457,
    0x0410, 0x0411, 0x0412, 0x0413, 0x0414, 0x0415, 0x0416, 0x0417,
    0x0418, 0x0419, 0x041A, 0x041B, 0x041C, 0x041D, 0x041E, 0x041F,
    0x0420, 0x0421, 0x0422, 0x0423, 0x0424, 0x0425, 0x0426, 0x0427,
    0x0428, 0x0429, 0x042A, 0x042B, 0x042C, 0x042D, 0x042E, 0x042F,
    0x0430, 0x0431, 0x0432, 0x0433, 0x0434, 0x0435, 0x0436, 0x0437,
    0x0438, 0x0439, 0x043A, 0x043B, 0x043C, 0x043D, 0x043E, 0x043F,
    0x0440, 0x0441, 0x0442, 0x0443, 0x0444, 0x0445, 0x0446, 0x0447,
    0x0448, 0x0449, 0x044A, 0x044B, 0x044C, 0x044D, 0x044E, 0x044F
};

const unsigned char qspCP1251ToUpperTable[] =
{
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
    0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
    0x60, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
    0x80, 0x81, 0x82, 0x81, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
    0x80, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x8A, 0x9B, 0x8C, 0x8D, 0x8E, 0x8F,
    0xA0, 0xA1, 0xA1, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
    0xB0, 0xB1, 0xB2, 0xB2, 0xA5, 0xB5, 0xB6, 0xB7, 0xA8, 0xB9, 0xAA, 0xBB, 0xA3, 0xBD, 0xBD, 0xAF,
    0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
    0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
    0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
    0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF
};

const unsigned char qspCP1251ToLowerTable[] =
{
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
    0x40, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
    0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
    0x90, 0x83, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x9A, 0x8B, 0x9C, 0x9D, 0x9E, 0x9F,
    0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
    0xA0, 0xA2, 0xA2, 0xBC, 0xA4, 0xB4, 0xA6, 0xA7, 0xB8, 0xA9, 0xBA, 0xAB, 0xAC, 0xAD, 0xAE, 0xBF,
    0xB0, 0xB1, 0xB3, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBE, 0xBE, 0xBF,
    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF,
    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF
};

const unsigned char qspKOI8RToUpperTable[] =
{
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
    0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
    0x60, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
    0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
    0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
    0xA0, 0xA1, 0xA2, 0xB3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
    0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF,
    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF
};

const unsigned char qspKOI8RToLowerTable[] =
{
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
    0x40, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
    0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
    0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
    0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
    0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
    0xB0, 0xB1, 0xB2, 0xA3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
    0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
    0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
    0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
    0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF
};

INLINE char qspDirectConvertSB(char ch, const unsigned char *table);
INLINE char qspReverseConvertSB(char ch, const unsigned char *table);
INLINE int qspDirectConvertUC(char ch, const int *table);
INLINE char qspReverseConvertUC(int ch, const int *table);

INLINE char qspDirectConvertSB(char ch, const unsigned char *table)
{
    unsigned char ch2 = (unsigned char)ch;
    return (ch2 >= 0x80 ? table[ch2 - 0x80] : ch);
}

INLINE char qspReverseConvertSB(char ch, const unsigned char *table)
{
    int i;
    unsigned char ch2 = (unsigned char)ch;
    if (ch2 < 0x80) return ch;
    for (i = 127; i >= 0; --i)
        if (table[i] == ch2) return (char)(i + 0x80);
    return 0x20;
}

INLINE int qspDirectConvertUC(char ch, const int *table)
{
    unsigned char ch2 = (unsigned char)ch;
    return (ch2 >= 0x80 ? table[ch2 - 0x80] : ch);
}

INLINE char qspReverseConvertUC(int ch, const int *table)
{
    int i;
    if (ch < 0x80) return (char)ch;
    for (i = 127; i >= 0; --i)
        if (table[i] == ch) return (char)(i + 0x80);
    return 0x20;
}

void *qspStringToFileData(QSPString s, QSP_BOOL isUCS2, int *dataSize)
{
    char *buf;
    QSP_CHAR *origBuf = s.Str;
    int bufSize, len = qspStrLen(s);
    bufSize = len * (isUCS2 ? 2 : 1);
    buf = (char *)malloc(bufSize);
    if (isUCS2)
    {
        unsigned short *uPtr = (unsigned short *)buf;
        while (--len >= 0)
            uPtr[len] = QSP_TO_GAME_UC(origBuf[len]);
    }
    else
    {
        while (--len >= 0)
            buf[len] = QSP_TO_GAME_SB(origBuf[len]);
    }
    *dataSize = bufSize;
    return buf;
}

QSPString qspStringFromFileData(void *data, int dataSize, QSP_BOOL isUCS2)
{
    QSP_CHAR *ret;
    int curLen, len = (isUCS2 ? dataSize / 2 : dataSize);
    if (!len) return qspNullString;
    ret = (QSP_CHAR *)malloc(len * sizeof(QSP_CHAR));
    curLen = len;
    if (isUCS2)
    {
        unsigned short *uPtr = (unsigned short *)data;
        while (--curLen >= 0)
            ret[curLen] = (QSP_CHAR)QSP_FROM_GAME_UC(uPtr[curLen]);
    }
    else
    {
        char *ptr = (char *)data;
        while (--curLen >= 0)
            ret[curLen] = (QSP_CHAR)QSP_FROM_GAME_SB(ptr[curLen]);
    }
    return qspStringFromLen(ret, len);
}

QSPString qspEncodeString(QSPString str, QSP_BOOL isUCS2)
{
    QSP_CHAR *origBuf, *buf;
    int curLen, len = qspStrLen(str);
    if (!len) return qspNullString;
    buf = (QSP_CHAR *)malloc(len * sizeof(QSP_CHAR));
    origBuf = str.Str;
    curLen = len;
    if (isUCS2)
    {
        unsigned short ch;
        while (--curLen >= 0)
        {
            ch = QSP_TO_GAME_UC(origBuf[curLen]);
            if (ch == QSP_CODREMOV)
                ch = (unsigned short)-QSP_CODREMOV;
            else
                ch -= QSP_CODREMOV;
            buf[curLen] = (QSP_CHAR)QSP_FROM_GAME_UC(ch);
        }
    }
    else
    {
        char ch;
        while (--curLen >= 0)
        {
            ch = QSP_TO_GAME_SB(origBuf[curLen]);
            if (ch == QSP_CODREMOV)
                ch = (char)-QSP_CODREMOV;
            else
                ch -= QSP_CODREMOV;
            buf[curLen] = (QSP_CHAR)QSP_FROM_GAME_SB(ch);
        }
    }
    return qspStringFromLen(buf, len);
}

QSPString qspDecodeString(QSPString str, QSP_BOOL isUCS2)
{
    QSP_CHAR *origBuf, *buf;
    int curLen, len = qspStrLen(str);
    if (!len) return qspNullString;
    buf = (QSP_CHAR *)malloc(len * sizeof(QSP_CHAR));
    origBuf = str.Str;
    curLen = len;
    if (isUCS2)
    {
        unsigned short ch;
        while (--curLen >= 0)
        {
            ch = QSP_TO_GAME_UC(origBuf[curLen]);
            if (ch == (unsigned short)-QSP_CODREMOV)
                ch = QSP_CODREMOV;
            else
                ch += QSP_CODREMOV;
            buf[curLen] = (QSP_CHAR)QSP_FROM_GAME_UC(ch);
        }
    }
    else
    {
        char ch;
        while (--curLen >= 0)
        {
            ch = QSP_TO_GAME_SB(origBuf[curLen]);
            if (ch == (char)-QSP_CODREMOV)
                ch = QSP_CODREMOV;
            else
                ch += QSP_CODREMOV;
            buf[curLen] = (QSP_CHAR)QSP_FROM_GAME_SB(ch);
        }
    }
    return qspStringFromLen(buf, len);
}

QSP_BIGINT qspReadEncodedIntVal(QSPString val, QSP_BOOL isUCS2)
{
    QSP_BIGINT num;
    QSPString temp = qspDecodeString(val, isUCS2);
    num = qspStrToNum(temp, 0);
    qspFreeString(&temp);
    return num;
}

void qspAppendEncodedIntVal(QSPBufString *s, QSP_BIGINT val, QSP_BOOL isUCS2)
{
    QSP_CHAR buf[QSP_MAX_BIGINT_LEN];
    QSPString temp, str = qspNumToStr(buf, val);
    temp = qspEncodeString(str, isUCS2);
    qspAddBufText(s, temp);
    qspFreeString(&temp);
    qspAddBufText(s, QSP_STATIC_STR(QSP_STRSDELIM));
}

void qspAppendEncodedStrVal(QSPBufString *s, QSPString val, QSP_BOOL isUCS2)
{
    if (val.Str)
    {
        QSPString temp = qspEncodeString(val, isUCS2);
        qspAddBufText(s, temp);
        qspFreeString(&temp);
    }
    qspAddBufText(s, QSP_STATIC_STR(QSP_STRSDELIM));
}

void qspAppendStrVal(QSPBufString *s, QSPString val)
{
    qspAddBufText(s, val);
    qspAddBufText(s, QSP_STATIC_STR(QSP_STRSDELIM));
}

void qspAppendEncodedVariant(QSPBufString *s, QSPVariant val, QSP_BOOL isUCS2)
{
    qspAppendEncodedIntVal(s, val.Type, isUCS2);
    switch (QSP_BASETYPE(val.Type))
    {
        case QSP_TYPE_TUPLE:
        {
            int i;
            QSPTuple tuple = QSP_TUPLE(val);
            qspAppendEncodedIntVal(s, tuple.Items, isUCS2);
            for (i = 0; i < tuple.Items; ++i)
                qspAppendEncodedVariant(s, tuple.Vals[i], isUCS2);
            break;
        }
        case QSP_TYPE_STR:
            qspAppendEncodedStrVal(s, QSP_STR(val), isUCS2);
            break;
        case QSP_TYPE_NUM:
            qspAppendEncodedIntVal(s, QSP_NUM(val), isUCS2);
            break;
    }
}

QSP_BOOL qspReadEncodedVariant(QSPString *strs, int strsCount, int *curIndex, QSP_BOOL isUCS2, QSPVariant *val)
{
    int type, ind = *curIndex;
    if (ind >= strsCount) return QSP_FALSE;
    type = qspReadEncodedIntVal(strs[ind++], isUCS2);
    if (type < 0 || type >= QSP_TYPE_DEFINED_TYPES) return QSP_FALSE; /* unsupported value type */
    switch (QSP_BASETYPE(type))
    {
        case QSP_TYPE_TUPLE:
        {
            int itemsCount;
            if (ind >= strsCount) return QSP_FALSE;
            itemsCount = qspReadEncodedIntVal(strs[ind++], isUCS2);
            if (itemsCount > 0)
            {
                int i;
                QSPVariant *vals = (QSPVariant *)malloc(itemsCount * sizeof(QSPVariant));
                for (i = 0; i < itemsCount; ++i)
                {
                    if (!qspReadEncodedVariant(strs, strsCount, &ind, isUCS2, vals + i))
                    {
                        qspFreeVariants(vals, i);
                        free(vals);
                        return QSP_FALSE;
                    }
                }
                QSP_PTUPLE(val).Vals = vals;
                QSP_PTUPLE(val).Items = itemsCount;
            }
            else
            {
                QSP_PTUPLE(val).Vals = 0;
                QSP_PTUPLE(val).Items = 0;
            }
            break;
        }
        case QSP_TYPE_STR:
            if (ind >= strsCount) return QSP_FALSE;
            QSP_PSTR(val) = qspDecodeString(strs[ind++], isUCS2);
            break;
        case QSP_TYPE_NUM:
            if (ind >= strsCount) return QSP_FALSE;
            QSP_PNUM(val) = qspReadEncodedIntVal(strs[ind++], isUCS2);
            break;
    }
    val->Type = (QSP_TINYINT)type;
    *curIndex = ind;
    return QSP_TRUE;
}
