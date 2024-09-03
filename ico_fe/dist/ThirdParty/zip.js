/* This file is automatically rebuilt by the Cesium build process. */
/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This program is based on JZlib 1.0.2 ymnk, JCraft,Inc.
 * JZlib is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */

// Global

const MAX_BITS$1 = 15;
const D_CODES = 30;
const BL_CODES = 19;

const LENGTH_CODES = 29;
const LITERALS = 256;
const L_CODES = (LITERALS + 1 + LENGTH_CODES);
const HEAP_SIZE = (2 * L_CODES + 1);

const END_BLOCK = 256;

// Bit length codes must not exceed MAX_BL_BITS bits
const MAX_BL_BITS = 7;

// repeat previous bit length 3-6 times (2 bits of repeat count)
const REP_3_6 = 16;

// repeat a zero length 3-10 times (3 bits of repeat count)
const REPZ_3_10 = 17;

// repeat a zero length 11-138 times (7 bits of repeat count)
const REPZ_11_138 = 18;

// The lengths of the bit length codes are sent in order of decreasing
// probability, to avoid transmitting the lengths for unused bit
// length codes.

const Buf_size = 8 * 2;

// JZlib version : "1.0.2"
const Z_DEFAULT_COMPRESSION = -1;

// compression strategy
const Z_FILTERED = 1;
const Z_HUFFMAN_ONLY = 2;
const Z_DEFAULT_STRATEGY = 0;

const Z_NO_FLUSH$1 = 0;
const Z_PARTIAL_FLUSH = 1;
const Z_FULL_FLUSH = 3;
const Z_FINISH$1 = 4;

const Z_OK$1 = 0;
const Z_STREAM_END$1 = 1;
const Z_NEED_DICT$1 = 2;
const Z_STREAM_ERROR$1 = -2;
const Z_DATA_ERROR$1 = -3;
const Z_BUF_ERROR$1 = -5;

// Tree

function extractArray(array) {
	return flatArray(array.map(([length, value]) => (new Array(length)).fill(value, 0, length)));
}

function flatArray(array) {
	return array.reduce((a, b) => a.concat(Array.isArray(b) ? flatArray(b) : b), []);
}

// see definition of array dist_code below
const _dist_code = [0, 1, 2, 3].concat(...extractArray([
	[2, 4], [2, 5], [4, 6], [4, 7], [8, 8], [8, 9], [16, 10], [16, 11], [32, 12], [32, 13], [64, 14], [64, 15], [2, 0], [1, 16],
	[1, 17], [2, 18], [2, 19], [4, 20], [4, 21], [8, 22], [8, 23], [16, 24], [16, 25], [32, 26], [32, 27], [64, 28], [64, 29]
]));

function Tree() {
	const that = this;

	// dyn_tree; // the dynamic tree
	// max_code; // largest code with non zero frequency
	// stat_desc; // the corresponding static tree

	// Compute the optimal bit lengths for a tree and update the total bit
	// length
	// for the current block.
	// IN assertion: the fields freq and dad are set, heap[heap_max] and
	// above are the tree nodes sorted by increasing frequency.
	// OUT assertions: the field len is set to the optimal bit length, the
	// array bl_count contains the frequencies for each bit length.
	// The length opt_len is updated; static_len is also updated if stree is
	// not null.
	function gen_bitlen(s) {
		const tree = that.dyn_tree;
		const stree = that.stat_desc.static_tree;
		const extra = that.stat_desc.extra_bits;
		const base = that.stat_desc.extra_base;
		const max_length = that.stat_desc.max_length;
		let h; // heap index
		let n, m; // iterate over the tree elements
		let bits; // bit length
		let xbits; // extra bits
		let f; // frequency
		let overflow = 0; // number of elements with bit length too large

		for (bits = 0; bits <= MAX_BITS$1; bits++)
			s.bl_count[bits] = 0;

		// In a first pass, compute the optimal bit lengths (which may
		// overflow in the case of the bit length tree).
		tree[s.heap[s.heap_max] * 2 + 1] = 0; // root of the heap

		for (h = s.heap_max + 1; h < HEAP_SIZE; h++) {
			n = s.heap[h];
			bits = tree[tree[n * 2 + 1] * 2 + 1] + 1;
			if (bits > max_length) {
				bits = max_length;
				overflow++;
			}
			tree[n * 2 + 1] = bits;
			// We overwrite tree[n*2+1] which is no longer needed

			if (n > that.max_code)
				continue; // not a leaf node

			s.bl_count[bits]++;
			xbits = 0;
			if (n >= base)
				xbits = extra[n - base];
			f = tree[n * 2];
			s.opt_len += f * (bits + xbits);
			if (stree)
				s.static_len += f * (stree[n * 2 + 1] + xbits);
		}
		if (overflow === 0)
			return;

		// This happens for example on obj2 and pic of the Calgary corpus
		// Find the first bit length which could increase:
		do {
			bits = max_length - 1;
			while (s.bl_count[bits] === 0)
				bits--;
			s.bl_count[bits]--; // move one leaf down the tree
			s.bl_count[bits + 1] += 2; // move one overflow item as its brother
			s.bl_count[max_length]--;
			// The brother of the overflow item also moves one step up,
			// but this does not affect bl_count[max_length]
			overflow -= 2;
		} while (overflow > 0);

		for (bits = max_length; bits !== 0; bits--) {
			n = s.bl_count[bits];
			while (n !== 0) {
				m = s.heap[--h];
				if (m > that.max_code)
					continue;
				if (tree[m * 2 + 1] != bits) {
					s.opt_len += (bits - tree[m * 2 + 1]) * tree[m * 2];
					tree[m * 2 + 1] = bits;
				}
				n--;
			}
		}
	}

	// Reverse the first len bits of a code, using straightforward code (a
	// faster
	// method would use a table)
	// IN assertion: 1 <= len <= 15
	function bi_reverse(code, // the value to invert
		len // its bit length
	) {
		let res = 0;
		do {
			res |= code & 1;
			code >>>= 1;
			res <<= 1;
		} while (--len > 0);
		return res >>> 1;
	}

	// Generate the codes for a given tree and bit counts (which need not be
	// optimal).
	// IN assertion: the array bl_count contains the bit length statistics for
	// the given tree and the field len is set for all tree elements.
	// OUT assertion: the field code is set for all tree elements of non
	// zero code length.
	function gen_codes(tree, // the tree to decorate
		max_code, // largest code with non zero frequency
		bl_count // number of codes at each bit length
	) {
		const next_code = []; // next code value for each
		// bit length
		let code = 0; // running code value
		let bits; // bit index
		let n; // code index
		let len;

		// The distribution counts are first used to generate the code values
		// without bit reversal.
		for (bits = 1; bits <= MAX_BITS$1; bits++) {
			next_code[bits] = code = ((code + bl_count[bits - 1]) << 1);
		}

		// Check that the bit counts in bl_count are consistent. The last code
		// must be all ones.
		// Assert (code + bl_count[MAX_BITS]-1 == (1<<MAX_BITS)-1,
		// "inconsistent bit counts");
		// Tracev((stderr,"gen_codes: max_code %d ", max_code));

		for (n = 0; n <= max_code; n++) {
			len = tree[n * 2 + 1];
			if (len === 0)
				continue;
			// Now reverse the bits
			tree[n * 2] = bi_reverse(next_code[len]++, len);
		}
	}

	// Construct one Huffman tree and assigns the code bit strings and lengths.
	// Update the total bit length for the current block.
	// IN assertion: the field freq is set for all tree elements.
	// OUT assertions: the fields len and code are set to the optimal bit length
	// and corresponding code. The length opt_len is updated; static_len is
	// also updated if stree is not null. The field max_code is set.
	that.build_tree = function (s) {
		const tree = that.dyn_tree;
		const stree = that.stat_desc.static_tree;
		const elems = that.stat_desc.elems;
		let n, m; // iterate over heap elements
		let max_code = -1; // largest code with non zero frequency
		let node; // new node being created

		// Construct the initial heap, with least frequent element in
		// heap[1]. The sons of heap[n] are heap[2*n] and heap[2*n+1].
		// heap[0] is not used.
		s.heap_len = 0;
		s.heap_max = HEAP_SIZE;

		for (n = 0; n < elems; n++) {
			if (tree[n * 2] !== 0) {
				s.heap[++s.heap_len] = max_code = n;
				s.depth[n] = 0;
			} else {
				tree[n * 2 + 1] = 0;
			}
		}

		// The pkzip format requires that at least one distance code exists,
		// and that at least one bit should be sent even if there is only one
		// possible code. So to avoid special checks later on we force at least
		// two codes of non zero frequency.
		while (s.heap_len < 2) {
			node = s.heap[++s.heap_len] = max_code < 2 ? ++max_code : 0;
			tree[node * 2] = 1;
			s.depth[node] = 0;
			s.opt_len--;
			if (stree)
				s.static_len -= stree[node * 2 + 1];
			// node is 0 or 1 so it does not have extra bits
		}
		that.max_code = max_code;

		// The elements heap[heap_len/2+1 .. heap_len] are leaves of the tree,
		// establish sub-heaps of increasing lengths:

		for (n = Math.floor(s.heap_len / 2); n >= 1; n--)
			s.pqdownheap(tree, n);

		// Construct the Huffman tree by repeatedly combining the least two
		// frequent nodes.

		node = elems; // next internal node of the tree
		do {
			// n = node of least frequency
			n = s.heap[1];
			s.heap[1] = s.heap[s.heap_len--];
			s.pqdownheap(tree, 1);
			m = s.heap[1]; // m = node of next least frequency

			s.heap[--s.heap_max] = n; // keep the nodes sorted by frequency
			s.heap[--s.heap_max] = m;

			// Create a new node father of n and m
			tree[node * 2] = (tree[n * 2] + tree[m * 2]);
			s.depth[node] = Math.max(s.depth[n], s.depth[m]) + 1;
			tree[n * 2 + 1] = tree[m * 2 + 1] = node;

			// and insert the new node in the heap
			s.heap[1] = node++;
			s.pqdownheap(tree, 1);
		} while (s.heap_len >= 2);

		s.heap[--s.heap_max] = s.heap[1];

		// At this point, the fields freq and dad are set. We can now
		// generate the bit lengths.

		gen_bitlen(s);

		// The field len is now set, we can generate the bit codes
		gen_codes(tree, that.max_code, s.bl_count);
	};

}

Tree._length_code = [0, 1, 2, 3, 4, 5, 6, 7].concat(...extractArray([
	[2, 8], [2, 9], [2, 10], [2, 11], [4, 12], [4, 13], [4, 14], [4, 15], [8, 16], [8, 17], [8, 18], [8, 19],
	[16, 20], [16, 21], [16, 22], [16, 23], [32, 24], [32, 25], [32, 26], [31, 27], [1, 28]]));

Tree.base_length = [0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 16, 20, 24, 28, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 0];

Tree.base_dist = [0, 1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256, 384, 512, 768, 1024, 1536, 2048, 3072, 4096, 6144, 8192, 12288, 16384,
	24576];

// Mapping from a distance to a distance code. dist is the distance - 1 and
// must not have side effects. _dist_code[256] and _dist_code[257] are never
// used.
Tree.d_code = function (dist) {
	return ((dist) < 256 ? _dist_code[dist] : _dist_code[256 + ((dist) >>> 7)]);
};

// extra bits for each length code
Tree.extra_lbits = [0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0];

// extra bits for each distance code
Tree.extra_dbits = [0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13];

// extra bits for each bit length code
Tree.extra_blbits = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 7];

Tree.bl_order = [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15];

// StaticTree

function StaticTree(static_tree, extra_bits, extra_base, elems, max_length) {
	const that = this;
	that.static_tree = static_tree;
	that.extra_bits = extra_bits;
	that.extra_base = extra_base;
	that.elems = elems;
	that.max_length = max_length;
}

const static_ltree2_first_part = [12, 140, 76, 204, 44, 172, 108, 236, 28, 156, 92, 220, 60, 188, 124, 252, 2, 130, 66, 194, 34, 162, 98, 226, 18, 146, 82,
	210, 50, 178, 114, 242, 10, 138, 74, 202, 42, 170, 106, 234, 26, 154, 90, 218, 58, 186, 122, 250, 6, 134, 70, 198, 38, 166, 102, 230, 22, 150, 86,
	214, 54, 182, 118, 246, 14, 142, 78, 206, 46, 174, 110, 238, 30, 158, 94, 222, 62, 190, 126, 254, 1, 129, 65, 193, 33, 161, 97, 225, 17, 145, 81,
	209, 49, 177, 113, 241, 9, 137, 73, 201, 41, 169, 105, 233, 25, 153, 89, 217, 57, 185, 121, 249, 5, 133, 69, 197, 37, 165, 101, 229, 21, 149, 85,
	213, 53, 181, 117, 245, 13, 141, 77, 205, 45, 173, 109, 237, 29, 157, 93, 221, 61, 189, 125, 253, 19, 275, 147, 403, 83, 339, 211, 467, 51, 307,
	179, 435, 115, 371, 243, 499, 11, 267, 139, 395, 75, 331, 203, 459, 43, 299, 171, 427, 107, 363, 235, 491, 27, 283, 155, 411, 91, 347, 219, 475,
	59, 315, 187, 443, 123, 379, 251, 507, 7, 263, 135, 391, 71, 327, 199, 455, 39, 295, 167, 423, 103, 359, 231, 487, 23, 279, 151, 407, 87, 343, 215,
	471, 55, 311, 183, 439, 119, 375, 247, 503, 15, 271, 143, 399, 79, 335, 207, 463, 47, 303, 175, 431, 111, 367, 239, 495, 31, 287, 159, 415, 95,
	351, 223, 479, 63, 319, 191, 447, 127, 383, 255, 511, 0, 64, 32, 96, 16, 80, 48, 112, 8, 72, 40, 104, 24, 88, 56, 120, 4, 68, 36, 100, 20, 84, 52,
	116, 3, 131, 67, 195, 35, 163, 99, 227];
const static_ltree2_second_part = extractArray([[144, 8], [112, 9], [24, 7], [8, 8]]);
StaticTree.static_ltree = flatArray(static_ltree2_first_part.map((value, index) => [value, static_ltree2_second_part[index]]));

const static_dtree_first_part = [0, 16, 8, 24, 4, 20, 12, 28, 2, 18, 10, 26, 6, 22, 14, 30, 1, 17, 9, 25, 5, 21, 13, 29, 3, 19, 11, 27, 7, 23];
const static_dtree_second_part = extractArray([[30, 5]]);
StaticTree.static_dtree = flatArray(static_dtree_first_part.map((value, index) => [value, static_dtree_second_part[index]]));

StaticTree.static_l_desc = new StaticTree(StaticTree.static_ltree, Tree.extra_lbits, LITERALS + 1, L_CODES, MAX_BITS$1);

StaticTree.static_d_desc = new StaticTree(StaticTree.static_dtree, Tree.extra_dbits, 0, D_CODES, MAX_BITS$1);

StaticTree.static_bl_desc = new StaticTree(null, Tree.extra_blbits, 0, BL_CODES, MAX_BL_BITS);

// Deflate

const MAX_MEM_LEVEL = 9;
const DEF_MEM_LEVEL = 8;

function Config(good_length, max_lazy, nice_length, max_chain, func) {
	const that = this;
	that.good_length = good_length;
	that.max_lazy = max_lazy;
	that.nice_length = nice_length;
	that.max_chain = max_chain;
	that.func = func;
}

const STORED$1 = 0;
const FAST = 1;
const SLOW = 2;
const config_table = [
	new Config(0, 0, 0, 0, STORED$1),
	new Config(4, 4, 8, 4, FAST),
	new Config(4, 5, 16, 8, FAST),
	new Config(4, 6, 32, 32, FAST),
	new Config(4, 4, 16, 16, SLOW),
	new Config(8, 16, 32, 32, SLOW),
	new Config(8, 16, 128, 128, SLOW),
	new Config(8, 32, 128, 256, SLOW),
	new Config(32, 128, 258, 1024, SLOW),
	new Config(32, 258, 258, 4096, SLOW)
];

const z_errmsg = ["need dictionary", // Z_NEED_DICT
	// 2
	"stream end", // Z_STREAM_END 1
	"", // Z_OK 0
	"", // Z_ERRNO (-1)
	"stream error", // Z_STREAM_ERROR (-2)
	"data error", // Z_DATA_ERROR (-3)
	"", // Z_MEM_ERROR (-4)
	"buffer error", // Z_BUF_ERROR (-5)
	"",// Z_VERSION_ERROR (-6)
	""];

// block not completed, need more input or more output
const NeedMore = 0;

// block flush performed
const BlockDone = 1;

// finish started, need only more output at next deflate
const FinishStarted = 2;

// finish done, accept no more input or output
const FinishDone = 3;

// preset dictionary flag in zlib header
const PRESET_DICT$1 = 0x20;

const INIT_STATE = 42;
const BUSY_STATE = 113;
const FINISH_STATE = 666;

// The deflate compression method
const Z_DEFLATED$1 = 8;

const STORED_BLOCK = 0;
const STATIC_TREES = 1;
const DYN_TREES = 2;

const MIN_MATCH = 3;
const MAX_MATCH = 258;
const MIN_LOOKAHEAD = (MAX_MATCH + MIN_MATCH + 1);

function smaller(tree, n, m, depth) {
	const tn2 = tree[n * 2];
	const tm2 = tree[m * 2];
	return (tn2 < tm2 || (tn2 == tm2 && depth[n] <= depth[m]));
}

function Deflate$1() {

	const that = this;
	let strm; // pointer back to this zlib stream
	let status; // as the name implies
	// pending_buf; // output still pending
	let pending_buf_size; // size of pending_buf
	// pending_out; // next pending byte to output to the stream
	// pending; // nb of bytes in the pending buffer

	// dist_buf; // buffer for distances
	// lc_buf; // buffer for literals or lengths
	// To simplify the code, dist_buf and lc_buf have the same number of elements.
	// To use different lengths, an extra flag array would be necessary.

	let last_flush; // value of flush param for previous deflate call

	let w_size; // LZ77 win size (32K by default)
	let w_bits; // log2(w_size) (8..16)
	let w_mask; // w_size - 1

	let win;
	// Sliding win. Input bytes are read into the second half of the win,
	// and move to the first half later to keep a dictionary of at least wSize
	// bytes. With this organization, matches are limited to a distance of
	// wSize-MAX_MATCH bytes, but this ensures that IO is always
	// performed with a length multiple of the block size. Also, it limits
	// the win size to 64K, which is quite useful on MSDOS.
	// To do: use the user input buffer as sliding win.

	let window_size;
	// Actual size of win: 2*wSize, except when the user input buffer
	// is directly used as sliding win.

	let prev;
	// Link to older string with same hash index. To limit the size of this
	// array to 64K, this link is maintained only for the last 32K strings.
	// An index in this array is thus a win index modulo 32K.

	let head; // Heads of the hash chains or NIL.

	let ins_h; // hash index of string to be inserted
	let hash_size; // number of elements in hash table
	let hash_bits; // log2(hash_size)
	let hash_mask; // hash_size-1

	// Number of bits by which ins_h must be shifted at each input
	// step. It must be such that after MIN_MATCH steps, the oldest
	// byte no longer takes part in the hash key, that is:
	// hash_shift * MIN_MATCH >= hash_bits
	let hash_shift;

	// Window position at the beginning of the current output block. Gets
	// negative when the win is moved backwards.

	let block_start;

	let match_length; // length of best match
	let prev_match; // previous match
	let match_available; // set if previous match exists
	let strstart; // start of string to insert
	let match_start; // start of matching string
	let lookahead; // number of valid bytes ahead in win

	// Length of the best match at previous step. Matches not greater than this
	// are discarded. This is used in the lazy match evaluation.
	let prev_length;

	// To speed up deflation, hash chains are never searched beyond this
	// length. A higher limit improves compression ratio but degrades the speed.
	let max_chain_length;

	// Attempt to find a better match only when the current match is strictly
	// smaller than this value. This mechanism is used only for compression
	// levels >= 4.
	let max_lazy_match;

	// Insert new strings in the hash table only if the match length is not
	// greater than this length. This saves time but degrades compression.
	// max_insert_length is used only for compression levels <= 3.

	let level; // compression level (1..9)
	let strategy; // favor or force Huffman coding

	// Use a faster search when the previous match is longer than this
	let good_match;

	// Stop searching when current match exceeds this
	let nice_match;

	let dyn_ltree; // literal and length tree
	let dyn_dtree; // distance tree
	let bl_tree; // Huffman tree for bit lengths

	const l_desc = new Tree(); // desc for literal tree
	const d_desc = new Tree(); // desc for distance tree
	const bl_desc = new Tree(); // desc for bit length tree

	// that.heap_len; // number of elements in the heap
	// that.heap_max; // element of largest frequency
	// The sons of heap[n] are heap[2*n] and heap[2*n+1]. heap[0] is not used.
	// The same heap array is used to build all trees.

	// Depth of each subtree used as tie breaker for trees of equal frequency
	that.depth = [];

	// Size of match buffer for literals/lengths. There are 4 reasons for
	// limiting lit_bufsize to 64K:
	// - frequencies can be kept in 16 bit counters
	// - if compression is not successful for the first block, all input
	// data is still in the win so we can still emit a stored block even
	// when input comes from standard input. (This can also be done for
	// all blocks if lit_bufsize is not greater than 32K.)
	// - if compression is not successful for a file smaller than 64K, we can
	// even emit a stored file instead of a stored block (saving 5 bytes).
	// This is applicable only for zip (not gzip or zlib).
	// - creating new Huffman trees less frequently may not provide fast
	// adaptation to changes in the input data statistics. (Take for
	// example a binary file with poorly compressible code followed by
	// a highly compressible string table.) Smaller buffer sizes give
	// fast adaptation but have of course the overhead of transmitting
	// trees more frequently.
	// - I can't count above 4
	let lit_bufsize;

	let last_lit; // running index in dist_buf and lc_buf

	// that.opt_len; // bit length of current block with optimal trees
	// that.static_len; // bit length of current block with static trees
	let matches; // number of string matches in current block
	let last_eob_len; // bit length of EOB code for last block

	// Output buffer. bits are inserted starting at the bottom (least
	// significant bits).
	let bi_buf;

	// Number of valid bits in bi_buf. All bits above the last valid bit
	// are always zero.
	let bi_valid;

	// number of codes at each bit length for an optimal tree
	that.bl_count = [];

	// heap used to build the Huffman trees
	that.heap = [];

	dyn_ltree = [];
	dyn_dtree = [];
	bl_tree = [];

	function lm_init() {
		window_size = 2 * w_size;

		head[hash_size - 1] = 0;
		for (let i = 0; i < hash_size - 1; i++) {
			head[i] = 0;
		}

		// Set the default configuration parameters:
		max_lazy_match = config_table[level].max_lazy;
		good_match = config_table[level].good_length;
		nice_match = config_table[level].nice_length;
		max_chain_length = config_table[level].max_chain;

		strstart = 0;
		block_start = 0;
		lookahead = 0;
		match_length = prev_length = MIN_MATCH - 1;
		match_available = 0;
		ins_h = 0;
	}

	function init_block() {
		let i;
		// Initialize the trees.
		for (i = 0; i < L_CODES; i++)
			dyn_ltree[i * 2] = 0;
		for (i = 0; i < D_CODES; i++)
			dyn_dtree[i * 2] = 0;
		for (i = 0; i < BL_CODES; i++)
			bl_tree[i * 2] = 0;

		dyn_ltree[END_BLOCK * 2] = 1;
		that.opt_len = that.static_len = 0;
		last_lit = matches = 0;
	}

	// Initialize the tree data structures for a new zlib stream.
	function tr_init() {

		l_desc.dyn_tree = dyn_ltree;
		l_desc.stat_desc = StaticTree.static_l_desc;

		d_desc.dyn_tree = dyn_dtree;
		d_desc.stat_desc = StaticTree.static_d_desc;

		bl_desc.dyn_tree = bl_tree;
		bl_desc.stat_desc = StaticTree.static_bl_desc;

		bi_buf = 0;
		bi_valid = 0;
		last_eob_len = 8; // enough lookahead for inflate

		// Initialize the first block of the first file:
		init_block();
	}

	// Restore the heap property by moving down the tree starting at node k,
	// exchanging a node with the smallest of its two sons if necessary,
	// stopping
	// when the heap property is re-established (each father smaller than its
	// two sons).
	that.pqdownheap = function (tree, // the tree to restore
		k // node to move down
	) {
		const heap = that.heap;
		const v = heap[k];
		let j = k << 1; // left son of k
		while (j <= that.heap_len) {
			// Set j to the smallest of the two sons:
			if (j < that.heap_len && smaller(tree, heap[j + 1], heap[j], that.depth)) {
				j++;
			}
			// Exit if v is smaller than both sons
			if (smaller(tree, v, heap[j], that.depth))
				break;

			// Exchange v with the smallest son
			heap[k] = heap[j];
			k = j;
			// And continue down the tree, setting j to the left son of k
			j <<= 1;
		}
		heap[k] = v;
	};

	// Scan a literal or distance tree to determine the frequencies of the codes
	// in the bit length tree.
	function scan_tree(tree,// the tree to be scanned
		max_code // and its largest code of non zero frequency
	) {
		let prevlen = -1; // last emitted length
		let curlen; // length of current code
		let nextlen = tree[0 * 2 + 1]; // length of next code
		let count = 0; // repeat count of the current code
		let max_count = 7; // max repeat count
		let min_count = 4; // min repeat count

		if (nextlen === 0) {
			max_count = 138;
			min_count = 3;
		}
		tree[(max_code + 1) * 2 + 1] = 0xffff; // guard

		for (let n = 0; n <= max_code; n++) {
			curlen = nextlen;
			nextlen = tree[(n + 1) * 2 + 1];
			if (++count < max_count && curlen == nextlen) {
				continue;
			} else if (count < min_count) {
				bl_tree[curlen * 2] += count;
			} else if (curlen !== 0) {
				if (curlen != prevlen)
					bl_tree[curlen * 2]++;
				bl_tree[REP_3_6 * 2]++;
			} else if (count <= 10) {
				bl_tree[REPZ_3_10 * 2]++;
			} else {
				bl_tree[REPZ_11_138 * 2]++;
			}
			count = 0;
			prevlen = curlen;
			if (nextlen === 0) {
				max_count = 138;
				min_count = 3;
			} else if (curlen == nextlen) {
				max_count = 6;
				min_count = 3;
			} else {
				max_count = 7;
				min_count = 4;
			}
		}
	}

	// Construct the Huffman tree for the bit lengths and return the index in
	// bl_order of the last bit length code to send.
	function build_bl_tree() {
		let max_blindex; // index of last bit length code of non zero freq

		// Determine the bit length frequencies for literal and distance trees
		scan_tree(dyn_ltree, l_desc.max_code);
		scan_tree(dyn_dtree, d_desc.max_code);

		// Build the bit length tree:
		bl_desc.build_tree(that);
		// opt_len now includes the length of the tree representations, except
		// the lengths of the bit lengths codes and the 5+5+4 bits for the
		// counts.

		// Determine the number of bit length codes to send. The pkzip format
		// requires that at least 4 bit length codes be sent. (appnote.txt says
		// 3 but the actual value used is 4.)
		for (max_blindex = BL_CODES - 1; max_blindex >= 3; max_blindex--) {
			if (bl_tree[Tree.bl_order[max_blindex] * 2 + 1] !== 0)
				break;
		}
		// Update opt_len to include the bit length tree and counts
		that.opt_len += 3 * (max_blindex + 1) + 5 + 5 + 4;

		return max_blindex;
	}

	// Output a byte on the stream.
	// IN assertion: there is enough room in pending_buf.
	function put_byte(p) {
		that.pending_buf[that.pending++] = p;
	}

	function put_short(w) {
		put_byte(w & 0xff);
		put_byte((w >>> 8) & 0xff);
	}

	function putShortMSB(b) {
		put_byte((b >> 8) & 0xff);
		put_byte((b & 0xff) & 0xff);
	}

	function send_bits(value, length) {
		let val;
		const len = length;
		if (bi_valid > Buf_size - len) {
			val = value;
			// bi_buf |= (val << bi_valid);
			bi_buf |= ((val << bi_valid) & 0xffff);
			put_short(bi_buf);
			bi_buf = val >>> (Buf_size - bi_valid);
			bi_valid += len - Buf_size;
		} else {
			// bi_buf |= (value) << bi_valid;
			bi_buf |= (((value) << bi_valid) & 0xffff);
			bi_valid += len;
		}
	}

	function send_code(c, tree) {
		const c2 = c * 2;
		send_bits(tree[c2] & 0xffff, tree[c2 + 1] & 0xffff);
	}

	// Send a literal or distance tree in compressed form, using the codes in
	// bl_tree.
	function send_tree(tree,// the tree to be sent
		max_code // and its largest code of non zero frequency
	) {
		let n; // iterates over all tree elements
		let prevlen = -1; // last emitted length
		let curlen; // length of current code
		let nextlen = tree[0 * 2 + 1]; // length of next code
		let count = 0; // repeat count of the current code
		let max_count = 7; // max repeat count
		let min_count = 4; // min repeat count

		if (nextlen === 0) {
			max_count = 138;
			min_count = 3;
		}

		for (n = 0; n <= max_code; n++) {
			curlen = nextlen;
			nextlen = tree[(n + 1) * 2 + 1];
			if (++count < max_count && curlen == nextlen) {
				continue;
			} else if (count < min_count) {
				do {
					send_code(curlen, bl_tree);
				} while (--count !== 0);
			} else if (curlen !== 0) {
				if (curlen != prevlen) {
					send_code(curlen, bl_tree);
					count--;
				}
				send_code(REP_3_6, bl_tree);
				send_bits(count - 3, 2);
			} else if (count <= 10) {
				send_code(REPZ_3_10, bl_tree);
				send_bits(count - 3, 3);
			} else {
				send_code(REPZ_11_138, bl_tree);
				send_bits(count - 11, 7);
			}
			count = 0;
			prevlen = curlen;
			if (nextlen === 0) {
				max_count = 138;
				min_count = 3;
			} else if (curlen == nextlen) {
				max_count = 6;
				min_count = 3;
			} else {
				max_count = 7;
				min_count = 4;
			}
		}
	}

	// Send the header for a block using dynamic Huffman trees: the counts, the
	// lengths of the bit length codes, the literal tree and the distance tree.
	// IN assertion: lcodes >= 257, dcodes >= 1, blcodes >= 4.
	function send_all_trees(lcodes, dcodes, blcodes) {
		let rank; // index in bl_order

		send_bits(lcodes - 257, 5); // not +255 as stated in appnote.txt
		send_bits(dcodes - 1, 5);
		send_bits(blcodes - 4, 4); // not -3 as stated in appnote.txt
		for (rank = 0; rank < blcodes; rank++) {
			send_bits(bl_tree[Tree.bl_order[rank] * 2 + 1], 3);
		}
		send_tree(dyn_ltree, lcodes - 1); // literal tree
		send_tree(dyn_dtree, dcodes - 1); // distance tree
	}

	// Flush the bit buffer, keeping at most 7 bits in it.
	function bi_flush() {
		if (bi_valid == 16) {
			put_short(bi_buf);
			bi_buf = 0;
			bi_valid = 0;
		} else if (bi_valid >= 8) {
			put_byte(bi_buf & 0xff);
			bi_buf >>>= 8;
			bi_valid -= 8;
		}
	}

	// Send one empty static block to give enough lookahead for inflate.
	// This takes 10 bits, of which 7 may remain in the bit buffer.
	// The current inflate code requires 9 bits of lookahead. If the
	// last two codes for the previous block (real code plus EOB) were coded
	// on 5 bits or less, inflate may have only 5+3 bits of lookahead to decode
	// the last real code. In this case we send two empty static blocks instead
	// of one. (There are no problems if the previous block is stored or fixed.)
	// To simplify the code, we assume the worst case of last real code encoded
	// on one bit only.
	function _tr_align() {
		send_bits(STATIC_TREES << 1, 3);
		send_code(END_BLOCK, StaticTree.static_ltree);

		bi_flush();

		// Of the 10 bits for the empty block, we have already sent
		// (10 - bi_valid) bits. The lookahead for the last real code (before
		// the EOB of the previous block) was thus at least one plus the length
		// of the EOB plus what we have just sent of the empty static block.
		if (1 + last_eob_len + 10 - bi_valid < 9) {
			send_bits(STATIC_TREES << 1, 3);
			send_code(END_BLOCK, StaticTree.static_ltree);
			bi_flush();
		}
		last_eob_len = 7;
	}

	// Save the match info and tally the frequency counts. Return true if
	// the current block must be flushed.
	function _tr_tally(dist, // distance of matched string
		lc // match length-MIN_MATCH or unmatched char (if dist==0)
	) {
		let out_length, in_length, dcode;
		that.dist_buf[last_lit] = dist;
		that.lc_buf[last_lit] = lc & 0xff;
		last_lit++;

		if (dist === 0) {
			// lc is the unmatched char
			dyn_ltree[lc * 2]++;
		} else {
			matches++;
			// Here, lc is the match length - MIN_MATCH
			dist--; // dist = match distance - 1
			dyn_ltree[(Tree._length_code[lc] + LITERALS + 1) * 2]++;
			dyn_dtree[Tree.d_code(dist) * 2]++;
		}

		if ((last_lit & 0x1fff) === 0 && level > 2) {
			// Compute an upper bound for the compressed length
			out_length = last_lit * 8;
			in_length = strstart - block_start;
			for (dcode = 0; dcode < D_CODES; dcode++) {
				out_length += dyn_dtree[dcode * 2] * (5 + Tree.extra_dbits[dcode]);
			}
			out_length >>>= 3;
			if ((matches < Math.floor(last_lit / 2)) && out_length < Math.floor(in_length / 2))
				return true;
		}

		return (last_lit == lit_bufsize - 1);
		// We avoid equality with lit_bufsize because of wraparound at 64K
		// on 16 bit machines and because stored blocks are restricted to
		// 64K-1 bytes.
	}

	// Send the block data compressed using the given Huffman trees
	function compress_block(ltree, dtree) {
		let dist; // distance of matched string
		let lc; // match length or unmatched char (if dist === 0)
		let lx = 0; // running index in dist_buf and lc_buf
		let code; // the code to send
		let extra; // number of extra bits to send

		if (last_lit !== 0) {
			do {
				dist = that.dist_buf[lx];
				lc = that.lc_buf[lx];
				lx++;

				if (dist === 0) {
					send_code(lc, ltree); // send a literal byte
				} else {
					// Here, lc is the match length - MIN_MATCH
					code = Tree._length_code[lc];

					send_code(code + LITERALS + 1, ltree); // send the length
					// code
					extra = Tree.extra_lbits[code];
					if (extra !== 0) {
						lc -= Tree.base_length[code];
						send_bits(lc, extra); // send the extra length bits
					}
					dist--; // dist is now the match distance - 1
					code = Tree.d_code(dist);

					send_code(code, dtree); // send the distance code
					extra = Tree.extra_dbits[code];
					if (extra !== 0) {
						dist -= Tree.base_dist[code];
						send_bits(dist, extra); // send the extra distance bits
					}
				} // literal or match pair ?
			} while (lx < last_lit);
		}

		send_code(END_BLOCK, ltree);
		last_eob_len = ltree[END_BLOCK * 2 + 1];
	}

	// Flush the bit buffer and align the output on a byte boundary
	function bi_windup() {
		if (bi_valid > 8) {
			put_short(bi_buf);
		} else if (bi_valid > 0) {
			put_byte(bi_buf & 0xff);
		}
		bi_buf = 0;
		bi_valid = 0;
	}

	// Copy a stored block, storing first the length and its
	// one's complement if requested.
	function copy_block(buf, // the input data
		len, // its length
		header // true if block header must be written
	) {
		bi_windup(); // align on byte boundary
		last_eob_len = 8; // enough lookahead for inflate

		if (header) {
			put_short(len);
			put_short(~len);
		}

		that.pending_buf.set(win.subarray(buf, buf + len), that.pending);
		that.pending += len;
	}

	// Send a stored block
	function _tr_stored_block(buf, // input block
		stored_len, // length of input block
		eof // true if this is the last block for a file
	) {
		send_bits((STORED_BLOCK << 1) + (eof ? 1 : 0), 3); // send block type
		copy_block(buf, stored_len, true); // with header
	}

	// Determine the best encoding for the current block: dynamic trees, static
	// trees or store, and output the encoded block to the zip file.
	function _tr_flush_block(buf, // input block, or NULL if too old
		stored_len, // length of input block
		eof // true if this is the last block for a file
	) {
		let opt_lenb, static_lenb;// opt_len and static_len in bytes
		let max_blindex = 0; // index of last bit length code of non zero freq

		// Build the Huffman trees unless a stored block is forced
		if (level > 0) {
			// Construct the literal and distance trees
			l_desc.build_tree(that);

			d_desc.build_tree(that);

			// At this point, opt_len and static_len are the total bit lengths
			// of
			// the compressed block data, excluding the tree representations.

			// Build the bit length tree for the above two trees, and get the
			// index
			// in bl_order of the last bit length code to send.
			max_blindex = build_bl_tree();

			// Determine the best encoding. Compute first the block length in
			// bytes
			opt_lenb = (that.opt_len + 3 + 7) >>> 3;
			static_lenb = (that.static_len + 3 + 7) >>> 3;

			if (static_lenb <= opt_lenb)
				opt_lenb = static_lenb;
		} else {
			opt_lenb = static_lenb = stored_len + 5; // force a stored block
		}

		if ((stored_len + 4 <= opt_lenb) && buf != -1) {
			// 4: two words for the lengths
			// The test buf != NULL is only necessary if LIT_BUFSIZE > WSIZE.
			// Otherwise we can't have processed more than WSIZE input bytes
			// since
			// the last block flush, because compression would have been
			// successful. If LIT_BUFSIZE <= WSIZE, it is never too late to
			// transform a block into a stored block.
			_tr_stored_block(buf, stored_len, eof);
		} else if (static_lenb == opt_lenb) {
			send_bits((STATIC_TREES << 1) + (eof ? 1 : 0), 3);
			compress_block(StaticTree.static_ltree, StaticTree.static_dtree);
		} else {
			send_bits((DYN_TREES << 1) + (eof ? 1 : 0), 3);
			send_all_trees(l_desc.max_code + 1, d_desc.max_code + 1, max_blindex + 1);
			compress_block(dyn_ltree, dyn_dtree);
		}

		// The above check is made mod 2^32, for files larger than 512 MB
		// and uLong implemented on 32 bits.

		init_block();

		if (eof) {
			bi_windup();
		}
	}

	function flush_block_only(eof) {
		_tr_flush_block(block_start >= 0 ? block_start : -1, strstart - block_start, eof);
		block_start = strstart;
		strm.flush_pending();
	}

	// Fill the win when the lookahead becomes insufficient.
	// Updates strstart and lookahead.
	//
	// IN assertion: lookahead < MIN_LOOKAHEAD
	// OUT assertions: strstart <= window_size-MIN_LOOKAHEAD
	// At least one byte has been read, or avail_in === 0; reads are
	// performed for at least two bytes (required for the zip translate_eol
	// option -- not supported here).
	function fill_window() {
		let n, m;
		let p;
		let more; // Amount of free space at the end of the win.

		do {
			more = (window_size - lookahead - strstart);

			// Deal with !@#$% 64K limit:
			if (more === 0 && strstart === 0 && lookahead === 0) {
				more = w_size;
			} else if (more == -1) {
				// Very unlikely, but possible on 16 bit machine if strstart ==
				// 0
				// and lookahead == 1 (input done one byte at time)
				more--;

				// If the win is almost full and there is insufficient
				// lookahead,
				// move the upper half to the lower one to make room in the
				// upper half.
			} else if (strstart >= w_size + w_size - MIN_LOOKAHEAD) {
				win.set(win.subarray(w_size, w_size + w_size), 0);

				match_start -= w_size;
				strstart -= w_size; // we now have strstart >= MAX_DIST
				block_start -= w_size;

				// Slide the hash table (could be avoided with 32 bit values
				// at the expense of memory usage). We slide even when level ==
				// 0
				// to keep the hash table consistent if we switch back to level
				// > 0
				// later. (Using level 0 permanently is not an optimal usage of
				// zlib, so we don't care about this pathological case.)

				n = hash_size;
				p = n;
				do {
					m = (head[--p] & 0xffff);
					head[p] = (m >= w_size ? m - w_size : 0);
				} while (--n !== 0);

				n = w_size;
				p = n;
				do {
					m = (prev[--p] & 0xffff);
					prev[p] = (m >= w_size ? m - w_size : 0);
					// If n is not on any hash chain, prev[n] is garbage but
					// its value will never be used.
				} while (--n !== 0);
				more += w_size;
			}

			if (strm.avail_in === 0)
				return;

			// If there was no sliding:
			// strstart <= WSIZE+MAX_DIST-1 && lookahead <= MIN_LOOKAHEAD - 1 &&
			// more == window_size - lookahead - strstart
			// => more >= window_size - (MIN_LOOKAHEAD-1 + WSIZE + MAX_DIST-1)
			// => more >= window_size - 2*WSIZE + 2
			// In the BIG_MEM or MMAP case (not yet supported),
			// window_size == input_size + MIN_LOOKAHEAD &&
			// strstart + s->lookahead <= input_size => more >= MIN_LOOKAHEAD.
			// Otherwise, window_size == 2*WSIZE so more >= 2.
			// If there was sliding, more >= WSIZE. So in all cases, more >= 2.

			n = strm.read_buf(win, strstart + lookahead, more);
			lookahead += n;

			// Initialize the hash value now that we have some input:
			if (lookahead >= MIN_MATCH) {
				ins_h = win[strstart] & 0xff;
				ins_h = (((ins_h) << hash_shift) ^ (win[strstart + 1] & 0xff)) & hash_mask;
			}
			// If the whole input has less than MIN_MATCH bytes, ins_h is
			// garbage,
			// but this is not important since only literal bytes will be
			// emitted.
		} while (lookahead < MIN_LOOKAHEAD && strm.avail_in !== 0);
	}

	// Copy without compression as much as possible from the input stream,
	// return
	// the current block state.
	// This function does not insert new strings in the dictionary since
	// uncompressible data is probably not useful. This function is used
	// only for the level=0 compression option.
	// NOTE: this function should be optimized to avoid extra copying from
	// win to pending_buf.
	function deflate_stored(flush) {
		// Stored blocks are limited to 0xffff bytes, pending_buf is limited
		// to pending_buf_size, and each stored block has a 5 byte header:

		let max_block_size = 0xffff;
		let max_start;

		if (max_block_size > pending_buf_size - 5) {
			max_block_size = pending_buf_size - 5;
		}

		// Copy as much as possible from input to output:
		// eslint-disable-next-line no-constant-condition
		while (true) {
			// Fill the win as much as possible:
			if (lookahead <= 1) {
				fill_window();
				if (lookahead === 0 && flush == Z_NO_FLUSH$1)
					return NeedMore;
				if (lookahead === 0)
					break; // flush the current block
			}

			strstart += lookahead;
			lookahead = 0;

			// Emit a stored block if pending_buf will be full:
			max_start = block_start + max_block_size;
			if (strstart === 0 || strstart >= max_start) {
				// strstart === 0 is possible when wraparound on 16-bit machine
				lookahead = (strstart - max_start);
				strstart = max_start;

				flush_block_only(false);
				if (strm.avail_out === 0)
					return NeedMore;

			}

			// Flush if we may have to slide, otherwise block_start may become
			// negative and the data will be gone:
			if (strstart - block_start >= w_size - MIN_LOOKAHEAD) {
				flush_block_only(false);
				if (strm.avail_out === 0)
					return NeedMore;
			}
		}

		flush_block_only(flush == Z_FINISH$1);
		if (strm.avail_out === 0)
			return (flush == Z_FINISH$1) ? FinishStarted : NeedMore;

		return flush == Z_FINISH$1 ? FinishDone : BlockDone;
	}

	function longest_match(cur_match) {
		let chain_length = max_chain_length; // max hash chain length
		let scan = strstart; // current string
		let match; // matched string
		let len; // length of current match
		let best_len = prev_length; // best match length so far
		const limit = strstart > (w_size - MIN_LOOKAHEAD) ? strstart - (w_size - MIN_LOOKAHEAD) : 0;
		let _nice_match = nice_match;

		// Stop when cur_match becomes <= limit. To simplify the code,
		// we prevent matches with the string of win index 0.

		const wmask = w_mask;

		const strend = strstart + MAX_MATCH;
		let scan_end1 = win[scan + best_len - 1];
		let scan_end = win[scan + best_len];

		// The code is optimized for HASH_BITS >= 8 and MAX_MATCH-2 multiple of
		// 16.
		// It is easy to get rid of this optimization if necessary.

		// Do not waste too much time if we already have a good match:
		if (prev_length >= good_match) {
			chain_length >>= 2;
		}

		// Do not look for matches beyond the end of the input. This is
		// necessary
		// to make deflate deterministic.
		if (_nice_match > lookahead)
			_nice_match = lookahead;

		do {
			match = cur_match;

			// Skip to next match if the match length cannot increase
			// or if the match length is less than 2:
			if (win[match + best_len] != scan_end || win[match + best_len - 1] != scan_end1 || win[match] != win[scan]
				|| win[++match] != win[scan + 1])
				continue;

			// The check at best_len-1 can be removed because it will be made
			// again later. (This heuristic is not always a win.)
			// It is not necessary to compare scan[2] and match[2] since they
			// are always equal when the other bytes match, given that
			// the hash keys are equal and that HASH_BITS >= 8.
			scan += 2;
			match++;

			// We check for insufficient lookahead only every 8th comparison;
			// the 256th check will be made at strstart+258.
			// eslint-disable-next-line no-empty
			do {
			} while (win[++scan] == win[++match] && win[++scan] == win[++match] && win[++scan] == win[++match]
			&& win[++scan] == win[++match] && win[++scan] == win[++match] && win[++scan] == win[++match]
			&& win[++scan] == win[++match] && win[++scan] == win[++match] && scan < strend);

			len = MAX_MATCH - (strend - scan);
			scan = strend - MAX_MATCH;

			if (len > best_len) {
				match_start = cur_match;
				best_len = len;
				if (len >= _nice_match)
					break;
				scan_end1 = win[scan + best_len - 1];
				scan_end = win[scan + best_len];
			}

		} while ((cur_match = (prev[cur_match & wmask] & 0xffff)) > limit && --chain_length !== 0);

		if (best_len <= lookahead)
			return best_len;
		return lookahead;
	}

	// Compress as much as possible from the input stream, return the current
	// block state.
	// This function does not perform lazy evaluation of matches and inserts
	// new strings in the dictionary only for unmatched strings or for short
	// matches. It is used only for the fast compression options.
	function deflate_fast(flush) {
		// short hash_head = 0; // head of the hash chain
		let hash_head = 0; // head of the hash chain
		let bflush; // set if current block must be flushed

		// eslint-disable-next-line no-constant-condition
		while (true) {
			// Make sure that we always have enough lookahead, except
			// at the end of the input file. We need MAX_MATCH bytes
			// for the next match, plus MIN_MATCH bytes to insert the
			// string following the next match.
			if (lookahead < MIN_LOOKAHEAD) {
				fill_window();
				if (lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH$1) {
					return NeedMore;
				}
				if (lookahead === 0)
					break; // flush the current block
			}

			// Insert the string win[strstart .. strstart+2] in the
			// dictionary, and set hash_head to the head of the hash chain:
			if (lookahead >= MIN_MATCH) {
				ins_h = (((ins_h) << hash_shift) ^ (win[(strstart) + (MIN_MATCH - 1)] & 0xff)) & hash_mask;

				// prev[strstart&w_mask]=hash_head=head[ins_h];
				hash_head = (head[ins_h] & 0xffff);
				prev[strstart & w_mask] = head[ins_h];
				head[ins_h] = strstart;
			}

			// Find the longest match, discarding those <= prev_length.
			// At this point we have always match_length < MIN_MATCH

			if (hash_head !== 0 && ((strstart - hash_head) & 0xffff) <= w_size - MIN_LOOKAHEAD) {
				// To simplify the code, we prevent matches with the string
				// of win index 0 (in particular we have to avoid a match
				// of the string with itself at the start of the input file).
				if (strategy != Z_HUFFMAN_ONLY) {
					match_length = longest_match(hash_head);
				}
				// longest_match() sets match_start
			}
			if (match_length >= MIN_MATCH) {
				// check_match(strstart, match_start, match_length);

				bflush = _tr_tally(strstart - match_start, match_length - MIN_MATCH);

				lookahead -= match_length;

				// Insert new strings in the hash table only if the match length
				// is not too large. This saves time but degrades compression.
				if (match_length <= max_lazy_match && lookahead >= MIN_MATCH) {
					match_length--; // string at strstart already in hash table
					do {
						strstart++;

						ins_h = ((ins_h << hash_shift) ^ (win[(strstart) + (MIN_MATCH - 1)] & 0xff)) & hash_mask;
						// prev[strstart&w_mask]=hash_head=head[ins_h];
						hash_head = (head[ins_h] & 0xffff);
						prev[strstart & w_mask] = head[ins_h];
						head[ins_h] = strstart;

						// strstart never exceeds WSIZE-MAX_MATCH, so there are
						// always MIN_MATCH bytes ahead.
					} while (--match_length !== 0);
					strstart++;
				} else {
					strstart += match_length;
					match_length = 0;
					ins_h = win[strstart] & 0xff;

					ins_h = (((ins_h) << hash_shift) ^ (win[strstart + 1] & 0xff)) & hash_mask;
					// If lookahead < MIN_MATCH, ins_h is garbage, but it does
					// not
					// matter since it will be recomputed at next deflate call.
				}
			} else {
				// No match, output a literal byte

				bflush = _tr_tally(0, win[strstart] & 0xff);
				lookahead--;
				strstart++;
			}
			if (bflush) {

				flush_block_only(false);
				if (strm.avail_out === 0)
					return NeedMore;
			}
		}

		flush_block_only(flush == Z_FINISH$1);
		if (strm.avail_out === 0) {
			if (flush == Z_FINISH$1)
				return FinishStarted;
			else
				return NeedMore;
		}
		return flush == Z_FINISH$1 ? FinishDone : BlockDone;
	}

	// Same as above, but achieves better compression. We use a lazy
	// evaluation for matches: a match is finally adopted only if there is
	// no better match at the next win position.
	function deflate_slow(flush) {
		// short hash_head = 0; // head of hash chain
		let hash_head = 0; // head of hash chain
		let bflush; // set if current block must be flushed
		let max_insert;

		// Process the input block.
		// eslint-disable-next-line no-constant-condition
		while (true) {
			// Make sure that we always have enough lookahead, except
			// at the end of the input file. We need MAX_MATCH bytes
			// for the next match, plus MIN_MATCH bytes to insert the
			// string following the next match.

			if (lookahead < MIN_LOOKAHEAD) {
				fill_window();
				if (lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH$1) {
					return NeedMore;
				}
				if (lookahead === 0)
					break; // flush the current block
			}

			// Insert the string win[strstart .. strstart+2] in the
			// dictionary, and set hash_head to the head of the hash chain:

			if (lookahead >= MIN_MATCH) {
				ins_h = (((ins_h) << hash_shift) ^ (win[(strstart) + (MIN_MATCH - 1)] & 0xff)) & hash_mask;
				// prev[strstart&w_mask]=hash_head=head[ins_h];
				hash_head = (head[ins_h] & 0xffff);
				prev[strstart & w_mask] = head[ins_h];
				head[ins_h] = strstart;
			}

			// Find the longest match, discarding those <= prev_length.
			prev_length = match_length;
			prev_match = match_start;
			match_length = MIN_MATCH - 1;

			if (hash_head !== 0 && prev_length < max_lazy_match && ((strstart - hash_head) & 0xffff) <= w_size - MIN_LOOKAHEAD) {
				// To simplify the code, we prevent matches with the string
				// of win index 0 (in particular we have to avoid a match
				// of the string with itself at the start of the input file).

				if (strategy != Z_HUFFMAN_ONLY) {
					match_length = longest_match(hash_head);
				}
				// longest_match() sets match_start

				if (match_length <= 5 && (strategy == Z_FILTERED || (match_length == MIN_MATCH && strstart - match_start > 4096))) {

					// If prev_match is also MIN_MATCH, match_start is garbage
					// but we will ignore the current match anyway.
					match_length = MIN_MATCH - 1;
				}
			}

			// If there was a match at the previous step and the current
			// match is not better, output the previous match:
			if (prev_length >= MIN_MATCH && match_length <= prev_length) {
				max_insert = strstart + lookahead - MIN_MATCH;
				// Do not insert strings in hash table beyond this.

				// check_match(strstart-1, prev_match, prev_length);

				bflush = _tr_tally(strstart - 1 - prev_match, prev_length - MIN_MATCH);

				// Insert in hash table all strings up to the end of the match.
				// strstart-1 and strstart are already inserted. If there is not
				// enough lookahead, the last two strings are not inserted in
				// the hash table.
				lookahead -= prev_length - 1;
				prev_length -= 2;
				do {
					if (++strstart <= max_insert) {
						ins_h = (((ins_h) << hash_shift) ^ (win[(strstart) + (MIN_MATCH - 1)] & 0xff)) & hash_mask;
						// prev[strstart&w_mask]=hash_head=head[ins_h];
						hash_head = (head[ins_h] & 0xffff);
						prev[strstart & w_mask] = head[ins_h];
						head[ins_h] = strstart;
					}
				} while (--prev_length !== 0);
				match_available = 0;
				match_length = MIN_MATCH - 1;
				strstart++;

				if (bflush) {
					flush_block_only(false);
					if (strm.avail_out === 0)
						return NeedMore;
				}
			} else if (match_available !== 0) {

				// If there was no match at the previous position, output a
				// single literal. If there was a match but the current match
				// is longer, truncate the previous match to a single literal.

				bflush = _tr_tally(0, win[strstart - 1] & 0xff);

				if (bflush) {
					flush_block_only(false);
				}
				strstart++;
				lookahead--;
				if (strm.avail_out === 0)
					return NeedMore;
			} else {
				// There is no previous match to compare with, wait for
				// the next step to decide.

				match_available = 1;
				strstart++;
				lookahead--;
			}
		}

		if (match_available !== 0) {
			bflush = _tr_tally(0, win[strstart - 1] & 0xff);
			match_available = 0;
		}
		flush_block_only(flush == Z_FINISH$1);

		if (strm.avail_out === 0) {
			if (flush == Z_FINISH$1)
				return FinishStarted;
			else
				return NeedMore;
		}

		return flush == Z_FINISH$1 ? FinishDone : BlockDone;
	}

	function deflateReset(strm) {
		strm.total_in = strm.total_out = 0;
		strm.msg = null; //

		that.pending = 0;
		that.pending_out = 0;

		status = BUSY_STATE;

		last_flush = Z_NO_FLUSH$1;

		tr_init();
		lm_init();
		return Z_OK$1;
	}

	that.deflateInit = function (strm, _level, bits, _method, memLevel, _strategy) {
		if (!_method)
			_method = Z_DEFLATED$1;
		if (!memLevel)
			memLevel = DEF_MEM_LEVEL;
		if (!_strategy)
			_strategy = Z_DEFAULT_STRATEGY;

		// byte[] my_version=ZLIB_VERSION;

		//
		// if (!version || version[0] != my_version[0]
		// || stream_size != sizeof(z_stream)) {
		// return Z_VERSION_ERROR;
		// }

		strm.msg = null;

		if (_level == Z_DEFAULT_COMPRESSION)
			_level = 6;

		if (memLevel < 1 || memLevel > MAX_MEM_LEVEL || _method != Z_DEFLATED$1 || bits < 9 || bits > 15 || _level < 0 || _level > 9 || _strategy < 0
			|| _strategy > Z_HUFFMAN_ONLY) {
			return Z_STREAM_ERROR$1;
		}

		strm.dstate = that;

		w_bits = bits;
		w_size = 1 << w_bits;
		w_mask = w_size - 1;

		hash_bits = memLevel + 7;
		hash_size = 1 << hash_bits;
		hash_mask = hash_size - 1;
		hash_shift = Math.floor((hash_bits + MIN_MATCH - 1) / MIN_MATCH);

		win = new Uint8Array(w_size * 2);
		prev = [];
		head = [];

		lit_bufsize = 1 << (memLevel + 6); // 16K elements by default

		that.pending_buf = new Uint8Array(lit_bufsize * 4);
		pending_buf_size = lit_bufsize * 4;

		that.dist_buf = new Uint16Array(lit_bufsize);
		that.lc_buf = new Uint8Array(lit_bufsize);

		level = _level;

		strategy = _strategy;

		return deflateReset(strm);
	};

	that.deflateEnd = function () {
		if (status != INIT_STATE && status != BUSY_STATE && status != FINISH_STATE) {
			return Z_STREAM_ERROR$1;
		}
		// Deallocate in reverse order of allocations:
		that.lc_buf = null;
		that.dist_buf = null;
		that.pending_buf = null;
		head = null;
		prev = null;
		win = null;
		// free
		that.dstate = null;
		return status == BUSY_STATE ? Z_DATA_ERROR$1 : Z_OK$1;
	};

	that.deflateParams = function (strm, _level, _strategy) {
		let err = Z_OK$1;

		if (_level == Z_DEFAULT_COMPRESSION) {
			_level = 6;
		}
		if (_level < 0 || _level > 9 || _strategy < 0 || _strategy > Z_HUFFMAN_ONLY) {
			return Z_STREAM_ERROR$1;
		}

		if (config_table[level].func != config_table[_level].func && strm.total_in !== 0) {
			// Flush the last buffer:
			err = strm.deflate(Z_PARTIAL_FLUSH);
		}

		if (level != _level) {
			level = _level;
			max_lazy_match = config_table[level].max_lazy;
			good_match = config_table[level].good_length;
			nice_match = config_table[level].nice_length;
			max_chain_length = config_table[level].max_chain;
		}
		strategy = _strategy;
		return err;
	};

	that.deflateSetDictionary = function (strm, dictionary, dictLength) {
		let length = dictLength;
		let n, index = 0;

		if (!dictionary || status != INIT_STATE)
			return Z_STREAM_ERROR$1;

		if (length < MIN_MATCH)
			return Z_OK$1;
		if (length > w_size - MIN_LOOKAHEAD) {
			length = w_size - MIN_LOOKAHEAD;
			index = dictLength - length; // use the tail of the dictionary
		}
		win.set(dictionary.subarray(index, index + length), 0);

		strstart = length;
		block_start = length;

		// Insert all strings in the hash table (except for the last two bytes).
		// s->lookahead stays null, so s->ins_h will be recomputed at the next
		// call of fill_window.

		ins_h = win[0] & 0xff;
		ins_h = (((ins_h) << hash_shift) ^ (win[1] & 0xff)) & hash_mask;

		for (n = 0; n <= length - MIN_MATCH; n++) {
			ins_h = (((ins_h) << hash_shift) ^ (win[(n) + (MIN_MATCH - 1)] & 0xff)) & hash_mask;
			prev[n & w_mask] = head[ins_h];
			head[ins_h] = n;
		}
		return Z_OK$1;
	};

	that.deflate = function (_strm, flush) {
		let i, header, level_flags, old_flush, bstate;

		if (flush > Z_FINISH$1 || flush < 0) {
			return Z_STREAM_ERROR$1;
		}

		if (!_strm.next_out || (!_strm.next_in && _strm.avail_in !== 0) || (status == FINISH_STATE && flush != Z_FINISH$1)) {
			_strm.msg = z_errmsg[Z_NEED_DICT$1 - (Z_STREAM_ERROR$1)];
			return Z_STREAM_ERROR$1;
		}
		if (_strm.avail_out === 0) {
			_strm.msg = z_errmsg[Z_NEED_DICT$1 - (Z_BUF_ERROR$1)];
			return Z_BUF_ERROR$1;
		}

		strm = _strm; // just in case
		old_flush = last_flush;
		last_flush = flush;

		// Write the zlib header
		if (status == INIT_STATE) {
			header = (Z_DEFLATED$1 + ((w_bits - 8) << 4)) << 8;
			level_flags = ((level - 1) & 0xff) >> 1;

			if (level_flags > 3)
				level_flags = 3;
			header |= (level_flags << 6);
			if (strstart !== 0)
				header |= PRESET_DICT$1;
			header += 31 - (header % 31);

			status = BUSY_STATE;
			putShortMSB(header);
		}

		// Flush as much pending output as possible
		if (that.pending !== 0) {
			strm.flush_pending();
			if (strm.avail_out === 0) {
				// console.log(" avail_out==0");
				// Since avail_out is 0, deflate will be called again with
				// more output space, but possibly with both pending and
				// avail_in equal to zero. There won't be anything to do,
				// but this is not an error situation so make sure we
				// return OK instead of BUF_ERROR at next call of deflate:
				last_flush = -1;
				return Z_OK$1;
			}

			// Make sure there is something to do and avoid duplicate
			// consecutive
			// flushes. For repeated and useless calls with Z_FINISH, we keep
			// returning Z_STREAM_END instead of Z_BUFF_ERROR.
		} else if (strm.avail_in === 0 && flush <= old_flush && flush != Z_FINISH$1) {
			strm.msg = z_errmsg[Z_NEED_DICT$1 - (Z_BUF_ERROR$1)];
			return Z_BUF_ERROR$1;
		}

		// User must not provide more input after the first FINISH:
		if (status == FINISH_STATE && strm.avail_in !== 0) {
			_strm.msg = z_errmsg[Z_NEED_DICT$1 - (Z_BUF_ERROR$1)];
			return Z_BUF_ERROR$1;
		}

		// Start a new block or continue the current one.
		if (strm.avail_in !== 0 || lookahead !== 0 || (flush != Z_NO_FLUSH$1 && status != FINISH_STATE)) {
			bstate = -1;
			switch (config_table[level].func) {
				case STORED$1:
					bstate = deflate_stored(flush);
					break;
				case FAST:
					bstate = deflate_fast(flush);
					break;
				case SLOW:
					bstate = deflate_slow(flush);
					break;
			}

			if (bstate == FinishStarted || bstate == FinishDone) {
				status = FINISH_STATE;
			}
			if (bstate == NeedMore || bstate == FinishStarted) {
				if (strm.avail_out === 0) {
					last_flush = -1; // avoid BUF_ERROR next call, see above
				}
				return Z_OK$1;
				// If flush != Z_NO_FLUSH && avail_out === 0, the next call
				// of deflate should use the same flush parameter to make sure
				// that the flush is complete. So we don't have to output an
				// empty block here, this will be done at next call. This also
				// ensures that for a very small output buffer, we emit at most
				// one empty block.
			}

			if (bstate == BlockDone) {
				if (flush == Z_PARTIAL_FLUSH) {
					_tr_align();
				} else { // FULL_FLUSH or SYNC_FLUSH
					_tr_stored_block(0, 0, false);
					// For a full flush, this empty block will be recognized
					// as a special marker by inflate_sync().
					if (flush == Z_FULL_FLUSH) {
						// state.head[s.hash_size-1]=0;
						for (i = 0; i < hash_size/*-1*/; i++)
							// forget history
							head[i] = 0;
					}
				}
				strm.flush_pending();
				if (strm.avail_out === 0) {
					last_flush = -1; // avoid BUF_ERROR at next call, see above
					return Z_OK$1;
				}
			}
		}

		if (flush != Z_FINISH$1)
			return Z_OK$1;
		return Z_STREAM_END$1;
	};
}

// ZStream

function ZStream$1() {
	const that = this;
	that.next_in_index = 0;
	that.next_out_index = 0;
	// that.next_in; // next input byte
	that.avail_in = 0; // number of bytes available at next_in
	that.total_in = 0; // total nb of input bytes read so far
	// that.next_out; // next output byte should be put there
	that.avail_out = 0; // remaining free space at next_out
	that.total_out = 0; // total nb of bytes output so far
	// that.msg;
	// that.dstate;
}

ZStream$1.prototype = {
	deflateInit: function (level, bits) {
		const that = this;
		that.dstate = new Deflate$1();
		if (!bits)
			bits = MAX_BITS$1;
		return that.dstate.deflateInit(that, level, bits);
	},

	deflate: function (flush) {
		const that = this;
		if (!that.dstate) {
			return Z_STREAM_ERROR$1;
		}
		return that.dstate.deflate(that, flush);
	},

	deflateEnd: function () {
		const that = this;
		if (!that.dstate)
			return Z_STREAM_ERROR$1;
		const ret = that.dstate.deflateEnd();
		that.dstate = null;
		return ret;
	},

	deflateParams: function (level, strategy) {
		const that = this;
		if (!that.dstate)
			return Z_STREAM_ERROR$1;
		return that.dstate.deflateParams(that, level, strategy);
	},

	deflateSetDictionary: function (dictionary, dictLength) {
		const that = this;
		if (!that.dstate)
			return Z_STREAM_ERROR$1;
		return that.dstate.deflateSetDictionary(that, dictionary, dictLength);
	},

	// Read a new buffer from the current input stream, update the
	// total number of bytes read. All deflate() input goes through
	// this function so some applications may wish to modify it to avoid
	// allocating a large strm->next_in buffer and copying from it.
	// (See also flush_pending()).
	read_buf: function (buf, start, size) {
		const that = this;
		let len = that.avail_in;
		if (len > size)
			len = size;
		if (len === 0)
			return 0;
		that.avail_in -= len;
		buf.set(that.next_in.subarray(that.next_in_index, that.next_in_index + len), start);
		that.next_in_index += len;
		that.total_in += len;
		return len;
	},

	// Flush as much pending output as possible. All deflate() output goes
	// through this function so some applications may wish to modify it
	// to avoid allocating a large strm->next_out buffer and copying into it.
	// (See also read_buf()).
	flush_pending: function () {
		const that = this;
		let len = that.dstate.pending;

		if (len > that.avail_out)
			len = that.avail_out;
		if (len === 0)
			return;

		// if (that.dstate.pending_buf.length <= that.dstate.pending_out || that.next_out.length <= that.next_out_index
		// || that.dstate.pending_buf.length < (that.dstate.pending_out + len) || that.next_out.length < (that.next_out_index +
		// len)) {
		// console.log(that.dstate.pending_buf.length + ", " + that.dstate.pending_out + ", " + that.next_out.length + ", " +
		// that.next_out_index + ", " + len);
		// console.log("avail_out=" + that.avail_out);
		// }

		that.next_out.set(that.dstate.pending_buf.subarray(that.dstate.pending_out, that.dstate.pending_out + len), that.next_out_index);

		that.next_out_index += len;
		that.dstate.pending_out += len;
		that.total_out += len;
		that.avail_out -= len;
		that.dstate.pending -= len;
		if (that.dstate.pending === 0) {
			that.dstate.pending_out = 0;
		}
	}
};

// Deflate

function ZipDeflate(options) {
	const that = this;
	const z = new ZStream$1();
	const bufsize = getMaximumCompressedSize$1(options && options.chunkSize ? options.chunkSize : 64 * 1024);
	const flush = Z_NO_FLUSH$1;
	const buf = new Uint8Array(bufsize);
	let level = options ? options.level : Z_DEFAULT_COMPRESSION;
	if (typeof level == "undefined")
		level = Z_DEFAULT_COMPRESSION;
	z.deflateInit(level);
	z.next_out = buf;

	that.append = function (data, onprogress) {
		let err, array, lastIndex = 0, bufferIndex = 0, bufferSize = 0;
		const buffers = [];
		if (!data.length)
			return;
		z.next_in_index = 0;
		z.next_in = data;
		z.avail_in = data.length;
		do {
			z.next_out_index = 0;
			z.avail_out = bufsize;
			err = z.deflate(flush);
			if (err != Z_OK$1)
				throw new Error("deflating: " + z.msg);
			if (z.next_out_index)
				if (z.next_out_index == bufsize)
					buffers.push(new Uint8Array(buf));
				else
					buffers.push(buf.slice(0, z.next_out_index));
			bufferSize += z.next_out_index;
			if (onprogress && z.next_in_index > 0 && z.next_in_index != lastIndex) {
				onprogress(z.next_in_index);
				lastIndex = z.next_in_index;
			}
		} while (z.avail_in > 0 || z.avail_out === 0);
		if (buffers.length > 1) {
			array = new Uint8Array(bufferSize);
			buffers.forEach(function (chunk) {
				array.set(chunk, bufferIndex);
				bufferIndex += chunk.length;
			});
		} else {
			array = buffers[0] || new Uint8Array(0);
		}
		return array;
	};
	that.flush = function () {
		let err, array, bufferIndex = 0, bufferSize = 0;
		const buffers = [];
		do {
			z.next_out_index = 0;
			z.avail_out = bufsize;
			err = z.deflate(Z_FINISH$1);
			if (err != Z_STREAM_END$1 && err != Z_OK$1)
				throw new Error("deflating: " + z.msg);
			if (bufsize - z.avail_out > 0)
				buffers.push(buf.slice(0, z.next_out_index));
			bufferSize += z.next_out_index;
		} while (z.avail_in > 0 || z.avail_out === 0);
		z.deflateEnd();
		array = new Uint8Array(bufferSize);
		buffers.forEach(function (chunk) {
			array.set(chunk, bufferIndex);
			bufferIndex += chunk.length;
		});
		return array;
	};
}

function getMaximumCompressedSize$1(uncompressedSize) {
	return uncompressedSize + (5 * (Math.floor(uncompressedSize / 16383) + 1));
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This program is based on JZlib 1.0.2 ymnk, JCraft,Inc.
 * JZlib is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */

// Global
const MAX_BITS = 15;

const Z_OK = 0;
const Z_STREAM_END = 1;
const Z_NEED_DICT = 2;
const Z_STREAM_ERROR = -2;
const Z_DATA_ERROR = -3;
const Z_MEM_ERROR = -4;
const Z_BUF_ERROR = -5;

const inflate_mask = [0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f, 0x000000ff, 0x000001ff, 0x000003ff,
	0x000007ff, 0x00000fff, 0x00001fff, 0x00003fff, 0x00007fff, 0x0000ffff];

const MANY = 1440;

// JZlib version : "1.0.2"
const Z_NO_FLUSH = 0;
const Z_FINISH = 4;

// InfTree
const fixed_bl = 9;
const fixed_bd = 5;

const fixed_tl = [96, 7, 256, 0, 8, 80, 0, 8, 16, 84, 8, 115, 82, 7, 31, 0, 8, 112, 0, 8, 48, 0, 9, 192, 80, 7, 10, 0, 8, 96, 0, 8, 32, 0, 9, 160, 0, 8, 0,
	0, 8, 128, 0, 8, 64, 0, 9, 224, 80, 7, 6, 0, 8, 88, 0, 8, 24, 0, 9, 144, 83, 7, 59, 0, 8, 120, 0, 8, 56, 0, 9, 208, 81, 7, 17, 0, 8, 104, 0, 8, 40,
	0, 9, 176, 0, 8, 8, 0, 8, 136, 0, 8, 72, 0, 9, 240, 80, 7, 4, 0, 8, 84, 0, 8, 20, 85, 8, 227, 83, 7, 43, 0, 8, 116, 0, 8, 52, 0, 9, 200, 81, 7, 13,
	0, 8, 100, 0, 8, 36, 0, 9, 168, 0, 8, 4, 0, 8, 132, 0, 8, 68, 0, 9, 232, 80, 7, 8, 0, 8, 92, 0, 8, 28, 0, 9, 152, 84, 7, 83, 0, 8, 124, 0, 8, 60,
	0, 9, 216, 82, 7, 23, 0, 8, 108, 0, 8, 44, 0, 9, 184, 0, 8, 12, 0, 8, 140, 0, 8, 76, 0, 9, 248, 80, 7, 3, 0, 8, 82, 0, 8, 18, 85, 8, 163, 83, 7,
	35, 0, 8, 114, 0, 8, 50, 0, 9, 196, 81, 7, 11, 0, 8, 98, 0, 8, 34, 0, 9, 164, 0, 8, 2, 0, 8, 130, 0, 8, 66, 0, 9, 228, 80, 7, 7, 0, 8, 90, 0, 8,
	26, 0, 9, 148, 84, 7, 67, 0, 8, 122, 0, 8, 58, 0, 9, 212, 82, 7, 19, 0, 8, 106, 0, 8, 42, 0, 9, 180, 0, 8, 10, 0, 8, 138, 0, 8, 74, 0, 9, 244, 80,
	7, 5, 0, 8, 86, 0, 8, 22, 192, 8, 0, 83, 7, 51, 0, 8, 118, 0, 8, 54, 0, 9, 204, 81, 7, 15, 0, 8, 102, 0, 8, 38, 0, 9, 172, 0, 8, 6, 0, 8, 134, 0,
	8, 70, 0, 9, 236, 80, 7, 9, 0, 8, 94, 0, 8, 30, 0, 9, 156, 84, 7, 99, 0, 8, 126, 0, 8, 62, 0, 9, 220, 82, 7, 27, 0, 8, 110, 0, 8, 46, 0, 9, 188, 0,
	8, 14, 0, 8, 142, 0, 8, 78, 0, 9, 252, 96, 7, 256, 0, 8, 81, 0, 8, 17, 85, 8, 131, 82, 7, 31, 0, 8, 113, 0, 8, 49, 0, 9, 194, 80, 7, 10, 0, 8, 97,
	0, 8, 33, 0, 9, 162, 0, 8, 1, 0, 8, 129, 0, 8, 65, 0, 9, 226, 80, 7, 6, 0, 8, 89, 0, 8, 25, 0, 9, 146, 83, 7, 59, 0, 8, 121, 0, 8, 57, 0, 9, 210,
	81, 7, 17, 0, 8, 105, 0, 8, 41, 0, 9, 178, 0, 8, 9, 0, 8, 137, 0, 8, 73, 0, 9, 242, 80, 7, 4, 0, 8, 85, 0, 8, 21, 80, 8, 258, 83, 7, 43, 0, 8, 117,
	0, 8, 53, 0, 9, 202, 81, 7, 13, 0, 8, 101, 0, 8, 37, 0, 9, 170, 0, 8, 5, 0, 8, 133, 0, 8, 69, 0, 9, 234, 80, 7, 8, 0, 8, 93, 0, 8, 29, 0, 9, 154,
	84, 7, 83, 0, 8, 125, 0, 8, 61, 0, 9, 218, 82, 7, 23, 0, 8, 109, 0, 8, 45, 0, 9, 186, 0, 8, 13, 0, 8, 141, 0, 8, 77, 0, 9, 250, 80, 7, 3, 0, 8, 83,
	0, 8, 19, 85, 8, 195, 83, 7, 35, 0, 8, 115, 0, 8, 51, 0, 9, 198, 81, 7, 11, 0, 8, 99, 0, 8, 35, 0, 9, 166, 0, 8, 3, 0, 8, 131, 0, 8, 67, 0, 9, 230,
	80, 7, 7, 0, 8, 91, 0, 8, 27, 0, 9, 150, 84, 7, 67, 0, 8, 123, 0, 8, 59, 0, 9, 214, 82, 7, 19, 0, 8, 107, 0, 8, 43, 0, 9, 182, 0, 8, 11, 0, 8, 139,
	0, 8, 75, 0, 9, 246, 80, 7, 5, 0, 8, 87, 0, 8, 23, 192, 8, 0, 83, 7, 51, 0, 8, 119, 0, 8, 55, 0, 9, 206, 81, 7, 15, 0, 8, 103, 0, 8, 39, 0, 9, 174,
	0, 8, 7, 0, 8, 135, 0, 8, 71, 0, 9, 238, 80, 7, 9, 0, 8, 95, 0, 8, 31, 0, 9, 158, 84, 7, 99, 0, 8, 127, 0, 8, 63, 0, 9, 222, 82, 7, 27, 0, 8, 111,
	0, 8, 47, 0, 9, 190, 0, 8, 15, 0, 8, 143, 0, 8, 79, 0, 9, 254, 96, 7, 256, 0, 8, 80, 0, 8, 16, 84, 8, 115, 82, 7, 31, 0, 8, 112, 0, 8, 48, 0, 9,
	193, 80, 7, 10, 0, 8, 96, 0, 8, 32, 0, 9, 161, 0, 8, 0, 0, 8, 128, 0, 8, 64, 0, 9, 225, 80, 7, 6, 0, 8, 88, 0, 8, 24, 0, 9, 145, 83, 7, 59, 0, 8,
	120, 0, 8, 56, 0, 9, 209, 81, 7, 17, 0, 8, 104, 0, 8, 40, 0, 9, 177, 0, 8, 8, 0, 8, 136, 0, 8, 72, 0, 9, 241, 80, 7, 4, 0, 8, 84, 0, 8, 20, 85, 8,
	227, 83, 7, 43, 0, 8, 116, 0, 8, 52, 0, 9, 201, 81, 7, 13, 0, 8, 100, 0, 8, 36, 0, 9, 169, 0, 8, 4, 0, 8, 132, 0, 8, 68, 0, 9, 233, 80, 7, 8, 0, 8,
	92, 0, 8, 28, 0, 9, 153, 84, 7, 83, 0, 8, 124, 0, 8, 60, 0, 9, 217, 82, 7, 23, 0, 8, 108, 0, 8, 44, 0, 9, 185, 0, 8, 12, 0, 8, 140, 0, 8, 76, 0, 9,
	249, 80, 7, 3, 0, 8, 82, 0, 8, 18, 85, 8, 163, 83, 7, 35, 0, 8, 114, 0, 8, 50, 0, 9, 197, 81, 7, 11, 0, 8, 98, 0, 8, 34, 0, 9, 165, 0, 8, 2, 0, 8,
	130, 0, 8, 66, 0, 9, 229, 80, 7, 7, 0, 8, 90, 0, 8, 26, 0, 9, 149, 84, 7, 67, 0, 8, 122, 0, 8, 58, 0, 9, 213, 82, 7, 19, 0, 8, 106, 0, 8, 42, 0, 9,
	181, 0, 8, 10, 0, 8, 138, 0, 8, 74, 0, 9, 245, 80, 7, 5, 0, 8, 86, 0, 8, 22, 192, 8, 0, 83, 7, 51, 0, 8, 118, 0, 8, 54, 0, 9, 205, 81, 7, 15, 0, 8,
	102, 0, 8, 38, 0, 9, 173, 0, 8, 6, 0, 8, 134, 0, 8, 70, 0, 9, 237, 80, 7, 9, 0, 8, 94, 0, 8, 30, 0, 9, 157, 84, 7, 99, 0, 8, 126, 0, 8, 62, 0, 9,
	221, 82, 7, 27, 0, 8, 110, 0, 8, 46, 0, 9, 189, 0, 8, 14, 0, 8, 142, 0, 8, 78, 0, 9, 253, 96, 7, 256, 0, 8, 81, 0, 8, 17, 85, 8, 131, 82, 7, 31, 0,
	8, 113, 0, 8, 49, 0, 9, 195, 80, 7, 10, 0, 8, 97, 0, 8, 33, 0, 9, 163, 0, 8, 1, 0, 8, 129, 0, 8, 65, 0, 9, 227, 80, 7, 6, 0, 8, 89, 0, 8, 25, 0, 9,
	147, 83, 7, 59, 0, 8, 121, 0, 8, 57, 0, 9, 211, 81, 7, 17, 0, 8, 105, 0, 8, 41, 0, 9, 179, 0, 8, 9, 0, 8, 137, 0, 8, 73, 0, 9, 243, 80, 7, 4, 0, 8,
	85, 0, 8, 21, 80, 8, 258, 83, 7, 43, 0, 8, 117, 0, 8, 53, 0, 9, 203, 81, 7, 13, 0, 8, 101, 0, 8, 37, 0, 9, 171, 0, 8, 5, 0, 8, 133, 0, 8, 69, 0, 9,
	235, 80, 7, 8, 0, 8, 93, 0, 8, 29, 0, 9, 155, 84, 7, 83, 0, 8, 125, 0, 8, 61, 0, 9, 219, 82, 7, 23, 0, 8, 109, 0, 8, 45, 0, 9, 187, 0, 8, 13, 0, 8,
	141, 0, 8, 77, 0, 9, 251, 80, 7, 3, 0, 8, 83, 0, 8, 19, 85, 8, 195, 83, 7, 35, 0, 8, 115, 0, 8, 51, 0, 9, 199, 81, 7, 11, 0, 8, 99, 0, 8, 35, 0, 9,
	167, 0, 8, 3, 0, 8, 131, 0, 8, 67, 0, 9, 231, 80, 7, 7, 0, 8, 91, 0, 8, 27, 0, 9, 151, 84, 7, 67, 0, 8, 123, 0, 8, 59, 0, 9, 215, 82, 7, 19, 0, 8,
	107, 0, 8, 43, 0, 9, 183, 0, 8, 11, 0, 8, 139, 0, 8, 75, 0, 9, 247, 80, 7, 5, 0, 8, 87, 0, 8, 23, 192, 8, 0, 83, 7, 51, 0, 8, 119, 0, 8, 55, 0, 9,
	207, 81, 7, 15, 0, 8, 103, 0, 8, 39, 0, 9, 175, 0, 8, 7, 0, 8, 135, 0, 8, 71, 0, 9, 239, 80, 7, 9, 0, 8, 95, 0, 8, 31, 0, 9, 159, 84, 7, 99, 0, 8,
	127, 0, 8, 63, 0, 9, 223, 82, 7, 27, 0, 8, 111, 0, 8, 47, 0, 9, 191, 0, 8, 15, 0, 8, 143, 0, 8, 79, 0, 9, 255];
const fixed_td = [80, 5, 1, 87, 5, 257, 83, 5, 17, 91, 5, 4097, 81, 5, 5, 89, 5, 1025, 85, 5, 65, 93, 5, 16385, 80, 5, 3, 88, 5, 513, 84, 5, 33, 92, 5,
	8193, 82, 5, 9, 90, 5, 2049, 86, 5, 129, 192, 5, 24577, 80, 5, 2, 87, 5, 385, 83, 5, 25, 91, 5, 6145, 81, 5, 7, 89, 5, 1537, 85, 5, 97, 93, 5,
	24577, 80, 5, 4, 88, 5, 769, 84, 5, 49, 92, 5, 12289, 82, 5, 13, 90, 5, 3073, 86, 5, 193, 192, 5, 24577];

// Tables for deflate from PKZIP's appnote.txt.
const cplens = [ // Copy lengths for literal codes 257..285
	3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0];

// see note #13 above about 258
const cplext = [ // Extra bits for literal codes 257..285
	0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 112, 112 // 112==invalid
];

const cpdist = [ // Copy offsets for distance codes 0..29
	1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577];

const cpdext = [ // Extra bits for distance codes
	0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13];

// If BMAX needs to be larger than 16, then h and x[] should be uLong.
const BMAX = 15; // maximum bit length of any code

function InfTree() {
	const that = this;

	let hn; // hufts used in space
	let v; // work area for huft_build
	let c; // bit length count table
	let r; // table entry for structure assignment
	let u; // table stack
	let x; // bit offsets, then code stack

	function huft_build(b, // code lengths in bits (all assumed <=
		// BMAX)
		bindex, n, // number of codes (assumed <= 288)
		s, // number of simple-valued codes (0..s-1)
		d, // list of base values for non-simple codes
		e, // list of extra bits for non-simple codes
		t, // result: starting table
		m, // maximum lookup bits, returns actual
		hp,// space for trees
		hn,// hufts used in space
		v // working area: values in order of bit length
	) {
		// Given a list of code lengths and a maximum table size, make a set of
		// tables to decode that set of codes. Return Z_OK on success,
		// Z_BUF_ERROR
		// if the given code set is incomplete (the tables are still built in
		// this
		// case), Z_DATA_ERROR if the input is invalid (an over-subscribed set
		// of
		// lengths), or Z_MEM_ERROR if not enough memory.

		let a; // counter for codes of length k
		let f; // i repeats in table every f entries
		let g; // maximum code length
		let h; // table level
		let i; // counter, current code
		let j; // counter
		let k; // number of bits in current code
		let l; // bits per table (returned in m)
		let mask; // (1 << w) - 1, to avoid cc -O bug on HP
		let p; // pointer into c[], b[], or v[]
		let q; // points to current table
		let w; // bits before this table == (l * h)
		let xp; // pointer into x
		let y; // number of dummy codes added
		let z; // number of entries in current table

		// Generate counts for each bit length

		p = 0;
		i = n;
		do {
			c[b[bindex + p]]++;
			p++;
			i--; // assume all entries <= BMAX
		} while (i !== 0);

		if (c[0] == n) { // null input--all zero length codes
			t[0] = -1;
			m[0] = 0;
			return Z_OK;
		}

		// Find minimum and maximum length, bound *m by those
		l = m[0];
		for (j = 1; j <= BMAX; j++)
			if (c[j] !== 0)
				break;
		k = j; // minimum code length
		if (l < j) {
			l = j;
		}
		for (i = BMAX; i !== 0; i--) {
			if (c[i] !== 0)
				break;
		}
		g = i; // maximum code length
		if (l > i) {
			l = i;
		}
		m[0] = l;

		// Adjust last length count to fill out codes, if needed
		for (y = 1 << j; j < i; j++, y <<= 1) {
			if ((y -= c[j]) < 0) {
				return Z_DATA_ERROR;
			}
		}
		if ((y -= c[i]) < 0) {
			return Z_DATA_ERROR;
		}
		c[i] += y;

		// Generate starting offsets into the value table for each length
		x[1] = j = 0;
		p = 1;
		xp = 2;
		while (--i !== 0) { // note that i == g from above
			x[xp] = (j += c[p]);
			xp++;
			p++;
		}

		// Make a table of values in order of bit lengths
		i = 0;
		p = 0;
		do {
			if ((j = b[bindex + p]) !== 0) {
				v[x[j]++] = i;
			}
			p++;
		} while (++i < n);
		n = x[g]; // set n to length of v

		// Generate the Huffman codes and for each, make the table entries
		x[0] = i = 0; // first Huffman code is zero
		p = 0; // grab values in bit order
		h = -1; // no tables yet--level -1
		w = -l; // bits decoded == (l * h)
		u[0] = 0; // just to keep compilers happy
		q = 0; // ditto
		z = 0; // ditto

		// go through the bit lengths (k already is bits in shortest code)
		for (; k <= g; k++) {
			a = c[k];
			while (a-- !== 0) {
				// here i is the Huffman code of length k bits for value *p
				// make tables up to required level
				while (k > w + l) {
					h++;
					w += l; // previous table always l bits
					// compute minimum size table less than or equal to l bits
					z = g - w;
					z = (z > l) ? l : z; // table size upper limit
					if ((f = 1 << (j = k - w)) > a + 1) { // try a k-w bit table
						// too few codes for
						// k-w bit table
						f -= a + 1; // deduct codes from patterns left
						xp = k;
						if (j < z) {
							while (++j < z) { // try smaller tables up to z bits
								if ((f <<= 1) <= c[++xp])
									break; // enough codes to use up j bits
								f -= c[xp]; // else deduct codes from patterns
							}
						}
					}
					z = 1 << j; // table entries for j-bit table

					// allocate new table
					if (hn[0] + z > MANY) { // (note: doesn't matter for fixed)
						return Z_DATA_ERROR; // overflow of MANY
					}
					u[h] = q = /* hp+ */hn[0]; // DEBUG
					hn[0] += z;

					// connect to last table, if there is one
					if (h !== 0) {
						x[h] = i; // save pattern for backing up
						r[0] = /* (byte) */j; // bits in this table
						r[1] = /* (byte) */l; // bits to dump before this table
						j = i >>> (w - l);
						r[2] = /* (int) */(q - u[h - 1] - j); // offset to this table
						hp.set(r, (u[h - 1] + j) * 3);
						// to
						// last
						// table
					} else {
						t[0] = q; // first table is returned result
					}
				}

				// set up table entry in r
				r[1] = /* (byte) */(k - w);
				if (p >= n) {
					r[0] = 128 + 64; // out of values--invalid code
				} else if (v[p] < s) {
					r[0] = /* (byte) */(v[p] < 256 ? 0 : 32 + 64); // 256 is
					// end-of-block
					r[2] = v[p++]; // simple code is just the value
				} else {
					r[0] = /* (byte) */(e[v[p] - s] + 16 + 64); // non-simple--look
					// up in lists
					r[2] = d[v[p++] - s];
				}

				// fill code-like entries with r
				f = 1 << (k - w);
				for (j = i >>> w; j < z; j += f) {
					hp.set(r, (q + j) * 3);
				}

				// backwards increment the k-bit code i
				for (j = 1 << (k - 1); (i & j) !== 0; j >>>= 1) {
					i ^= j;
				}
				i ^= j;

				// backup over finished tables
				mask = (1 << w) - 1; // needed on HP, cc -O bug
				while ((i & mask) != x[h]) {
					h--; // don't need to update q
					w -= l;
					mask = (1 << w) - 1;
				}
			}
		}
		// Return Z_BUF_ERROR if we were given an incomplete table
		return y !== 0 && g != 1 ? Z_BUF_ERROR : Z_OK;
	}

	function initWorkArea(vsize) {
		let i;
		if (!hn) {
			hn = []; // []; //new Array(1);
			v = []; // new Array(vsize);
			c = new Int32Array(BMAX + 1); // new Array(BMAX + 1);
			r = []; // new Array(3);
			u = new Int32Array(BMAX); // new Array(BMAX);
			x = new Int32Array(BMAX + 1); // new Array(BMAX + 1);
		}
		if (v.length < vsize) {
			v = []; // new Array(vsize);
		}
		for (i = 0; i < vsize; i++) {
			v[i] = 0;
		}
		for (i = 0; i < BMAX + 1; i++) {
			c[i] = 0;
		}
		for (i = 0; i < 3; i++) {
			r[i] = 0;
		}
		// for(int i=0; i<BMAX; i++){u[i]=0;}
		u.set(c.subarray(0, BMAX), 0);
		// for(int i=0; i<BMAX+1; i++){x[i]=0;}
		x.set(c.subarray(0, BMAX + 1), 0);
	}

	that.inflate_trees_bits = function (c, // 19 code lengths
		bb, // bits tree desired/actual depth
		tb, // bits tree result
		hp, // space for trees
		z // for messages
	) {
		let result;
		initWorkArea(19);
		hn[0] = 0;
		result = huft_build(c, 0, 19, 19, null, null, tb, bb, hp, hn, v);

		if (result == Z_DATA_ERROR) {
			z.msg = "oversubscribed dynamic bit lengths tree";
		} else if (result == Z_BUF_ERROR || bb[0] === 0) {
			z.msg = "incomplete dynamic bit lengths tree";
			result = Z_DATA_ERROR;
		}
		return result;
	};

	that.inflate_trees_dynamic = function (nl, // number of literal/length codes
		nd, // number of distance codes
		c, // that many (total) code lengths
		bl, // literal desired/actual bit depth
		bd, // distance desired/actual bit depth
		tl, // literal/length tree result
		td, // distance tree result
		hp, // space for trees
		z // for messages
	) {
		let result;

		// build literal/length tree
		initWorkArea(288);
		hn[0] = 0;
		result = huft_build(c, 0, nl, 257, cplens, cplext, tl, bl, hp, hn, v);
		if (result != Z_OK || bl[0] === 0) {
			if (result == Z_DATA_ERROR) {
				z.msg = "oversubscribed literal/length tree";
			} else if (result != Z_MEM_ERROR) {
				z.msg = "incomplete literal/length tree";
				result = Z_DATA_ERROR;
			}
			return result;
		}

		// build distance tree
		initWorkArea(288);
		result = huft_build(c, nl, nd, 0, cpdist, cpdext, td, bd, hp, hn, v);

		if (result != Z_OK || (bd[0] === 0 && nl > 257)) {
			if (result == Z_DATA_ERROR) {
				z.msg = "oversubscribed distance tree";
			} else if (result == Z_BUF_ERROR) {
				z.msg = "incomplete distance tree";
				result = Z_DATA_ERROR;
			} else if (result != Z_MEM_ERROR) {
				z.msg = "empty distance tree with lengths";
				result = Z_DATA_ERROR;
			}
			return result;
		}

		return Z_OK;
	};

}

InfTree.inflate_trees_fixed = function (bl, // literal desired/actual bit depth
	bd, // distance desired/actual bit depth
	tl,// literal/length tree result
	td// distance tree result
) {
	bl[0] = fixed_bl;
	bd[0] = fixed_bd;
	tl[0] = fixed_tl;
	td[0] = fixed_td;
	return Z_OK;
};

// InfCodes

// waiting for "i:"=input,
// "o:"=output,
// "x:"=nothing
const START = 0; // x: set up for LEN
const LEN = 1; // i: get length/literal/eob next
const LENEXT = 2; // i: getting length extra (have base)
const DIST = 3; // i: get distance next
const DISTEXT = 4;// i: getting distance extra
const COPY = 5; // o: copying bytes in win, waiting
// for space
const LIT = 6; // o: got literal, waiting for output
// space
const WASH = 7; // o: got eob, possibly still output
// waiting
const END = 8; // x: got eob and all data flushed
const BADCODE = 9;// x: got error

function InfCodes() {
	const that = this;

	let mode; // current inflate_codes mode

	// mode dependent information
	let len = 0;

	let tree; // pointer into tree
	let tree_index = 0;
	let need = 0; // bits needed

	let lit = 0;

	// if EXT or COPY, where and how much
	let get = 0; // bits to get for extra
	let dist = 0; // distance back to copy from

	let lbits = 0; // ltree bits decoded per branch
	let dbits = 0; // dtree bits decoder per branch
	let ltree; // literal/length/eob tree
	let ltree_index = 0; // literal/length/eob tree
	let dtree; // distance tree
	let dtree_index = 0; // distance tree

	// Called with number of bytes left to write in win at least 258
	// (the maximum string length) and number of input bytes available
	// at least ten. The ten bytes are six bytes for the longest length/
	// distance pair plus four bytes for overloading the bit buffer.

	function inflate_fast(bl, bd, tl, tl_index, td, td_index, s, z) {
		let t; // temporary pointer
		let tp; // temporary pointer
		let tp_index; // temporary pointer
		let e; // extra bits or operation
		let b; // bit buffer
		let k; // bits in bit buffer
		let p; // input data pointer
		let n; // bytes available there
		let q; // output win write pointer
		let m; // bytes to end of win or read pointer
		let ml; // mask for literal/length tree
		let md; // mask for distance tree
		let c; // bytes to copy
		let d; // distance back to copy from
		let r; // copy source pointer

		let tp_index_t_3; // (tp_index+t)*3

		// load input, output, bit values
		p = z.next_in_index;
		n = z.avail_in;
		b = s.bitb;
		k = s.bitk;
		q = s.write;
		m = q < s.read ? s.read - q - 1 : s.end - q;

		// initialize masks
		ml = inflate_mask[bl];
		md = inflate_mask[bd];

		// do until not enough input or output space for fast loop
		do { // assume called with m >= 258 && n >= 10
			// get literal/length code
			while (k < (20)) { // max bits for literal/length code
				n--;
				b |= (z.read_byte(p++) & 0xff) << k;
				k += 8;
			}

			t = b & ml;
			tp = tl;
			tp_index = tl_index;
			tp_index_t_3 = (tp_index + t) * 3;
			if ((e = tp[tp_index_t_3]) === 0) {
				b >>= (tp[tp_index_t_3 + 1]);
				k -= (tp[tp_index_t_3 + 1]);

				s.win[q++] = /* (byte) */tp[tp_index_t_3 + 2];
				m--;
				continue;
			}
			do {

				b >>= (tp[tp_index_t_3 + 1]);
				k -= (tp[tp_index_t_3 + 1]);

				if ((e & 16) !== 0) {
					e &= 15;
					c = tp[tp_index_t_3 + 2] + (/* (int) */b & inflate_mask[e]);

					b >>= e;
					k -= e;

					// decode distance base of block to copy
					while (k < (15)) { // max bits for distance code
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					t = b & md;
					tp = td;
					tp_index = td_index;
					tp_index_t_3 = (tp_index + t) * 3;
					e = tp[tp_index_t_3];

					do {

						b >>= (tp[tp_index_t_3 + 1]);
						k -= (tp[tp_index_t_3 + 1]);

						if ((e & 16) !== 0) {
							// get extra bits to add to distance base
							e &= 15;
							while (k < (e)) { // get extra bits (up to 13)
								n--;
								b |= (z.read_byte(p++) & 0xff) << k;
								k += 8;
							}

							d = tp[tp_index_t_3 + 2] + (b & inflate_mask[e]);

							b >>= (e);
							k -= (e);

							// do the copy
							m -= c;
							if (q >= d) { // offset before dest
								// just copy
								r = q - d;
								if (q - r > 0 && 2 > (q - r)) {
									s.win[q++] = s.win[r++]; // minimum
									// count is
									// three,
									s.win[q++] = s.win[r++]; // so unroll
									// loop a
									// little
									c -= 2;
								} else {
									s.win.set(s.win.subarray(r, r + 2), q);
									q += 2;
									r += 2;
									c -= 2;
								}
							} else { // else offset after destination
								r = q - d;
								do {
									r += s.end; // force pointer in win
								} while (r < 0); // covers invalid distances
								e = s.end - r;
								if (c > e) { // if source crosses,
									c -= e; // wrapped copy
									if (q - r > 0 && e > (q - r)) {
										do {
											s.win[q++] = s.win[r++];
										} while (--e !== 0);
									} else {
										s.win.set(s.win.subarray(r, r + e), q);
										q += e;
										r += e;
										e = 0;
									}
									r = 0; // copy rest from start of win
								}

							}

							// copy all or what's left
							if (q - r > 0 && c > (q - r)) {
								do {
									s.win[q++] = s.win[r++];
								} while (--c !== 0);
							} else {
								s.win.set(s.win.subarray(r, r + c), q);
								q += c;
								r += c;
								c = 0;
							}
							break;
						} else if ((e & 64) === 0) {
							t += tp[tp_index_t_3 + 2];
							t += (b & inflate_mask[e]);
							tp_index_t_3 = (tp_index + t) * 3;
							e = tp[tp_index_t_3];
						} else {
							z.msg = "invalid distance code";

							c = z.avail_in - n;
							c = (k >> 3) < c ? k >> 3 : c;
							n += c;
							p -= c;
							k -= c << 3;

							s.bitb = b;
							s.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							s.write = q;

							return Z_DATA_ERROR;
						}
						// eslint-disable-next-line no-constant-condition
					} while (true);
					break;
				}

				if ((e & 64) === 0) {
					t += tp[tp_index_t_3 + 2];
					t += (b & inflate_mask[e]);
					tp_index_t_3 = (tp_index + t) * 3;
					if ((e = tp[tp_index_t_3]) === 0) {

						b >>= (tp[tp_index_t_3 + 1]);
						k -= (tp[tp_index_t_3 + 1]);

						s.win[q++] = /* (byte) */tp[tp_index_t_3 + 2];
						m--;
						break;
					}
				} else if ((e & 32) !== 0) {

					c = z.avail_in - n;
					c = (k >> 3) < c ? k >> 3 : c;
					n += c;
					p -= c;
					k -= c << 3;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;

					return Z_STREAM_END;
				} else {
					z.msg = "invalid literal/length code";

					c = z.avail_in - n;
					c = (k >> 3) < c ? k >> 3 : c;
					n += c;
					p -= c;
					k -= c << 3;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;

					return Z_DATA_ERROR;
				}
				// eslint-disable-next-line no-constant-condition
			} while (true);
		} while (m >= 258 && n >= 10);

		// not enough input or output--restore pointers and return
		c = z.avail_in - n;
		c = (k >> 3) < c ? k >> 3 : c;
		n += c;
		p -= c;
		k -= c << 3;

		s.bitb = b;
		s.bitk = k;
		z.avail_in = n;
		z.total_in += p - z.next_in_index;
		z.next_in_index = p;
		s.write = q;

		return Z_OK;
	}

	that.init = function (bl, bd, tl, tl_index, td, td_index) {
		mode = START;
		lbits = /* (byte) */bl;
		dbits = /* (byte) */bd;
		ltree = tl;
		ltree_index = tl_index;
		dtree = td;
		dtree_index = td_index;
		tree = null;
	};

	that.proc = function (s, z, r) {
		let j; // temporary storage
		let tindex; // temporary pointer
		let e; // extra bits or operation
		let b = 0; // bit buffer
		let k = 0; // bits in bit buffer
		let p = 0; // input data pointer
		let n; // bytes available there
		let q; // output win write pointer
		let m; // bytes to end of win or read pointer
		let f; // pointer to copy strings from

		// copy input/output information to locals (UPDATE macro restores)
		p = z.next_in_index;
		n = z.avail_in;
		b = s.bitb;
		k = s.bitk;
		q = s.write;
		m = q < s.read ? s.read - q - 1 : s.end - q;

		// process input and output based on current state
		// eslint-disable-next-line no-constant-condition
		while (true) {
			switch (mode) {
				// waiting for "i:"=input, "o:"=output, "x:"=nothing
				case START: // x: set up for LEN
					if (m >= 258 && n >= 10) {

						s.bitb = b;
						s.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						s.write = q;
						r = inflate_fast(lbits, dbits, ltree, ltree_index, dtree, dtree_index, s, z);

						p = z.next_in_index;
						n = z.avail_in;
						b = s.bitb;
						k = s.bitk;
						q = s.write;
						m = q < s.read ? s.read - q - 1 : s.end - q;

						if (r != Z_OK) {
							mode = r == Z_STREAM_END ? WASH : BADCODE;
							break;
						}
					}
					need = lbits;
					tree = ltree;
					tree_index = ltree_index;

					mode = LEN;
				/* falls through */
				case LEN: // i: get length/literal/eob next
					j = need;

					while (k < (j)) {
						if (n !== 0)
							r = Z_OK;
						else {

							s.bitb = b;
							s.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							s.write = q;
							return s.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					tindex = (tree_index + (b & inflate_mask[j])) * 3;

					b >>>= (tree[tindex + 1]);
					k -= (tree[tindex + 1]);

					e = tree[tindex];

					if (e === 0) { // literal
						lit = tree[tindex + 2];
						mode = LIT;
						break;
					}
					if ((e & 16) !== 0) { // length
						get = e & 15;
						len = tree[tindex + 2];
						mode = LENEXT;
						break;
					}
					if ((e & 64) === 0) { // next table
						need = e;
						tree_index = tindex / 3 + tree[tindex + 2];
						break;
					}
					if ((e & 32) !== 0) { // end of block
						mode = WASH;
						break;
					}
					mode = BADCODE; // invalid code
					z.msg = "invalid literal/length code";
					r = Z_DATA_ERROR;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;
					return s.inflate_flush(z, r);

				case LENEXT: // i: getting length extra (have base)
					j = get;

					while (k < (j)) {
						if (n !== 0)
							r = Z_OK;
						else {

							s.bitb = b;
							s.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							s.write = q;
							return s.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					len += (b & inflate_mask[j]);

					b >>= j;
					k -= j;

					need = dbits;
					tree = dtree;
					tree_index = dtree_index;
					mode = DIST;
				/* falls through */
				case DIST: // i: get distance next
					j = need;

					while (k < (j)) {
						if (n !== 0)
							r = Z_OK;
						else {

							s.bitb = b;
							s.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							s.write = q;
							return s.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					tindex = (tree_index + (b & inflate_mask[j])) * 3;

					b >>= tree[tindex + 1];
					k -= tree[tindex + 1];

					e = (tree[tindex]);
					if ((e & 16) !== 0) { // distance
						get = e & 15;
						dist = tree[tindex + 2];
						mode = DISTEXT;
						break;
					}
					if ((e & 64) === 0) { // next table
						need = e;
						tree_index = tindex / 3 + tree[tindex + 2];
						break;
					}
					mode = BADCODE; // invalid code
					z.msg = "invalid distance code";
					r = Z_DATA_ERROR;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;
					return s.inflate_flush(z, r);

				case DISTEXT: // i: getting distance extra
					j = get;

					while (k < (j)) {
						if (n !== 0)
							r = Z_OK;
						else {

							s.bitb = b;
							s.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							s.write = q;
							return s.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					dist += (b & inflate_mask[j]);

					b >>= j;
					k -= j;

					mode = COPY;
				/* falls through */
				case COPY: // o: copying bytes in win, waiting for space
					f = q - dist;
					while (f < 0) { // modulo win size-"while" instead
						f += s.end; // of "if" handles invalid distances
					}
					while (len !== 0) {

						if (m === 0) {
							if (q == s.end && s.read !== 0) {
								q = 0;
								m = q < s.read ? s.read - q - 1 : s.end - q;
							}
							if (m === 0) {
								s.write = q;
								r = s.inflate_flush(z, r);
								q = s.write;
								m = q < s.read ? s.read - q - 1 : s.end - q;

								if (q == s.end && s.read !== 0) {
									q = 0;
									m = q < s.read ? s.read - q - 1 : s.end - q;
								}

								if (m === 0) {
									s.bitb = b;
									s.bitk = k;
									z.avail_in = n;
									z.total_in += p - z.next_in_index;
									z.next_in_index = p;
									s.write = q;
									return s.inflate_flush(z, r);
								}
							}
						}

						s.win[q++] = s.win[f++];
						m--;

						if (f == s.end)
							f = 0;
						len--;
					}
					mode = START;
					break;
				case LIT: // o: got literal, waiting for output space
					if (m === 0) {
						if (q == s.end && s.read !== 0) {
							q = 0;
							m = q < s.read ? s.read - q - 1 : s.end - q;
						}
						if (m === 0) {
							s.write = q;
							r = s.inflate_flush(z, r);
							q = s.write;
							m = q < s.read ? s.read - q - 1 : s.end - q;

							if (q == s.end && s.read !== 0) {
								q = 0;
								m = q < s.read ? s.read - q - 1 : s.end - q;
							}
							if (m === 0) {
								s.bitb = b;
								s.bitk = k;
								z.avail_in = n;
								z.total_in += p - z.next_in_index;
								z.next_in_index = p;
								s.write = q;
								return s.inflate_flush(z, r);
							}
						}
					}
					r = Z_OK;

					s.win[q++] = /* (byte) */lit;
					m--;

					mode = START;
					break;
				case WASH: // o: got eob, possibly more output
					if (k > 7) { // return unused byte, if any
						k -= 8;
						n++;
						p--; // can always return one
					}

					s.write = q;
					r = s.inflate_flush(z, r);
					q = s.write;
					m = q < s.read ? s.read - q - 1 : s.end - q;

					if (s.read != s.write) {
						s.bitb = b;
						s.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						s.write = q;
						return s.inflate_flush(z, r);
					}
					mode = END;
				/* falls through */
				case END:
					r = Z_STREAM_END;
					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;
					return s.inflate_flush(z, r);

				case BADCODE: // x: got error

					r = Z_DATA_ERROR;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;
					return s.inflate_flush(z, r);

				default:
					r = Z_STREAM_ERROR;

					s.bitb = b;
					s.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					s.write = q;
					return s.inflate_flush(z, r);
			}
		}
	};

	that.free = function () {
		// ZFREE(z, c);
	};

}

// InfBlocks

// Table for deflate from PKZIP's appnote.txt.
const border = [ // Order of the bit length code lengths
	16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15];

const TYPE = 0; // get type bits (3, including end bit)
const LENS = 1; // get lengths for stored
const STORED = 2;// processing stored block
const TABLE = 3; // get table lengths
const BTREE = 4; // get bit lengths tree for a dynamic
// block
const DTREE = 5; // get length, distance trees for a
// dynamic block
const CODES = 6; // processing fixed or dynamic block
const DRY = 7; // output remaining win bytes
const DONELOCKS = 8; // finished last block, done
const BADBLOCKS = 9; // ot a data error--stuck here

function InfBlocks(z, w) {
	const that = this;

	let mode = TYPE; // current inflate_block mode

	let left = 0; // if STORED, bytes left to copy

	let table = 0; // table lengths (14 bits)
	let index = 0; // index into blens (or border)
	let blens; // bit lengths of codes
	const bb = [0]; // bit length tree depth
	const tb = [0]; // bit length decoding tree

	const codes = new InfCodes(); // if CODES, current state

	let last = 0; // true if this block is the last block

	let hufts = new Int32Array(MANY * 3); // single malloc for tree space
	const check = 0; // check on output
	const inftree = new InfTree();

	that.bitk = 0; // bits in bit buffer
	that.bitb = 0; // bit buffer
	that.win = new Uint8Array(w); // sliding win
	that.end = w; // one byte after sliding win
	that.read = 0; // win read pointer
	that.write = 0; // win write pointer

	that.reset = function (z, c) {
		if (c)
			c[0] = check;
		// if (mode == BTREE || mode == DTREE) {
		// }
		if (mode == CODES) {
			codes.free(z);
		}
		mode = TYPE;
		that.bitk = 0;
		that.bitb = 0;
		that.read = that.write = 0;
	};

	that.reset(z, null);

	// copy as much as possible from the sliding win to the output area
	that.inflate_flush = function (z, r) {
		let n;
		let p;
		let q;

		// local copies of source and destination pointers
		p = z.next_out_index;
		q = that.read;

		// compute number of bytes to copy as far as end of win
		n = /* (int) */((q <= that.write ? that.write : that.end) - q);
		if (n > z.avail_out)
			n = z.avail_out;
		if (n !== 0 && r == Z_BUF_ERROR)
			r = Z_OK;

		// update counters
		z.avail_out -= n;
		z.total_out += n;

		// copy as far as end of win
		z.next_out.set(that.win.subarray(q, q + n), p);
		p += n;
		q += n;

		// see if more to copy at beginning of win
		if (q == that.end) {
			// wrap pointers
			q = 0;
			if (that.write == that.end)
				that.write = 0;

			// compute bytes to copy
			n = that.write - q;
			if (n > z.avail_out)
				n = z.avail_out;
			if (n !== 0 && r == Z_BUF_ERROR)
				r = Z_OK;

			// update counters
			z.avail_out -= n;
			z.total_out += n;

			// copy
			z.next_out.set(that.win.subarray(q, q + n), p);
			p += n;
			q += n;
		}

		// update pointers
		z.next_out_index = p;
		that.read = q;

		// done
		return r;
	};

	that.proc = function (z, r) {
		let t; // temporary storage
		let b; // bit buffer
		let k; // bits in bit buffer
		let p; // input data pointer
		let n; // bytes available there
		let q; // output win write pointer
		let m; // bytes to end of win or read pointer

		let i;

		// copy input/output information to locals (UPDATE macro restores)
		// {
		p = z.next_in_index;
		n = z.avail_in;
		b = that.bitb;
		k = that.bitk;
		// }
		// {
		q = that.write;
		m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);
		// }

		// process input based on current state
		// DEBUG dtree
		// eslint-disable-next-line no-constant-condition
		while (true) {
			let bl, bd, tl, td, bl_, bd_, tl_, td_;
			switch (mode) {
				case TYPE:

					while (k < (3)) {
						if (n !== 0) {
							r = Z_OK;
						} else {
							that.bitb = b;
							that.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							that.write = q;
							return that.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}
					t = /* (int) */(b & 7);
					last = t & 1;

					switch (t >>> 1) {
						case 0: // stored
							// {
							b >>>= (3);
							k -= (3);
							// }
							t = k & 7; // go to byte boundary

							// {
							b >>>= (t);
							k -= (t);
							// }
							mode = LENS; // get length of stored block
							break;
						case 1: // fixed
							// {
							bl = []; // new Array(1);
							bd = []; // new Array(1);
							tl = [[]]; // new Array(1);
							td = [[]]; // new Array(1);

							InfTree.inflate_trees_fixed(bl, bd, tl, td);
							codes.init(bl[0], bd[0], tl[0], 0, td[0], 0);
							// }

							// {
							b >>>= (3);
							k -= (3);
							// }

							mode = CODES;
							break;
						case 2: // dynamic

							// {
							b >>>= (3);
							k -= (3);
							// }

							mode = TABLE;
							break;
						case 3: // illegal

							// {
							b >>>= (3);
							k -= (3);
							// }
							mode = BADBLOCKS;
							z.msg = "invalid block type";
							r = Z_DATA_ERROR;

							that.bitb = b;
							that.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							that.write = q;
							return that.inflate_flush(z, r);
					}
					break;
				case LENS:

					while (k < (32)) {
						if (n !== 0) {
							r = Z_OK;
						} else {
							that.bitb = b;
							that.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							that.write = q;
							return that.inflate_flush(z, r);
						}
						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					if ((((~b) >>> 16) & 0xffff) != (b & 0xffff)) {
						mode = BADBLOCKS;
						z.msg = "invalid stored block lengths";
						r = Z_DATA_ERROR;

						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}
					left = (b & 0xffff);
					b = k = 0; // dump bits
					mode = left !== 0 ? STORED : (last !== 0 ? DRY : TYPE);
					break;
				case STORED:
					if (n === 0) {
						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}

					if (m === 0) {
						if (q == that.end && that.read !== 0) {
							q = 0;
							m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);
						}
						if (m === 0) {
							that.write = q;
							r = that.inflate_flush(z, r);
							q = that.write;
							m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);
							if (q == that.end && that.read !== 0) {
								q = 0;
								m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);
							}
							if (m === 0) {
								that.bitb = b;
								that.bitk = k;
								z.avail_in = n;
								z.total_in += p - z.next_in_index;
								z.next_in_index = p;
								that.write = q;
								return that.inflate_flush(z, r);
							}
						}
					}
					r = Z_OK;

					t = left;
					if (t > n)
						t = n;
					if (t > m)
						t = m;
					that.win.set(z.read_buf(p, t), q);
					p += t;
					n -= t;
					q += t;
					m -= t;
					if ((left -= t) !== 0)
						break;
					mode = last !== 0 ? DRY : TYPE;
					break;
				case TABLE:

					while (k < (14)) {
						if (n !== 0) {
							r = Z_OK;
						} else {
							that.bitb = b;
							that.bitk = k;
							z.avail_in = n;
							z.total_in += p - z.next_in_index;
							z.next_in_index = p;
							that.write = q;
							return that.inflate_flush(z, r);
						}

						n--;
						b |= (z.read_byte(p++) & 0xff) << k;
						k += 8;
					}

					table = t = (b & 0x3fff);
					if ((t & 0x1f) > 29 || ((t >> 5) & 0x1f) > 29) {
						mode = BADBLOCKS;
						z.msg = "too many length or distance symbols";
						r = Z_DATA_ERROR;

						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}
					t = 258 + (t & 0x1f) + ((t >> 5) & 0x1f);
					if (!blens || blens.length < t) {
						blens = []; // new Array(t);
					} else {
						for (i = 0; i < t; i++) {
							blens[i] = 0;
						}
					}

					// {
					b >>>= (14);
					k -= (14);
					// }

					index = 0;
					mode = BTREE;
				/* falls through */
				case BTREE:
					while (index < 4 + (table >>> 10)) {
						while (k < (3)) {
							if (n !== 0) {
								r = Z_OK;
							} else {
								that.bitb = b;
								that.bitk = k;
								z.avail_in = n;
								z.total_in += p - z.next_in_index;
								z.next_in_index = p;
								that.write = q;
								return that.inflate_flush(z, r);
							}
							n--;
							b |= (z.read_byte(p++) & 0xff) << k;
							k += 8;
						}

						blens[border[index++]] = b & 7;

						// {
						b >>>= (3);
						k -= (3);
						// }
					}

					while (index < 19) {
						blens[border[index++]] = 0;
					}

					bb[0] = 7;
					t = inftree.inflate_trees_bits(blens, bb, tb, hufts, z);
					if (t != Z_OK) {
						r = t;
						if (r == Z_DATA_ERROR) {
							blens = null;
							mode = BADBLOCKS;
						}

						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}

					index = 0;
					mode = DTREE;
				/* falls through */
				case DTREE:
					// eslint-disable-next-line no-constant-condition
					while (true) {
						t = table;
						if (index >= 258 + (t & 0x1f) + ((t >> 5) & 0x1f)) {
							break;
						}

						let j, c;

						t = bb[0];

						while (k < (t)) {
							if (n !== 0) {
								r = Z_OK;
							} else {
								that.bitb = b;
								that.bitk = k;
								z.avail_in = n;
								z.total_in += p - z.next_in_index;
								z.next_in_index = p;
								that.write = q;
								return that.inflate_flush(z, r);
							}
							n--;
							b |= (z.read_byte(p++) & 0xff) << k;
							k += 8;
						}

						// if (tb[0] == -1) {
						// System.err.println("null...");
						// }

						t = hufts[(tb[0] + (b & inflate_mask[t])) * 3 + 1];
						c = hufts[(tb[0] + (b & inflate_mask[t])) * 3 + 2];

						if (c < 16) {
							b >>>= (t);
							k -= (t);
							blens[index++] = c;
						} else { // c == 16..18
							i = c == 18 ? 7 : c - 14;
							j = c == 18 ? 11 : 3;

							while (k < (t + i)) {
								if (n !== 0) {
									r = Z_OK;
								} else {
									that.bitb = b;
									that.bitk = k;
									z.avail_in = n;
									z.total_in += p - z.next_in_index;
									z.next_in_index = p;
									that.write = q;
									return that.inflate_flush(z, r);
								}
								n--;
								b |= (z.read_byte(p++) & 0xff) << k;
								k += 8;
							}

							b >>>= (t);
							k -= (t);

							j += (b & inflate_mask[i]);

							b >>>= (i);
							k -= (i);

							i = index;
							t = table;
							if (i + j > 258 + (t & 0x1f) + ((t >> 5) & 0x1f) || (c == 16 && i < 1)) {
								blens = null;
								mode = BADBLOCKS;
								z.msg = "invalid bit length repeat";
								r = Z_DATA_ERROR;

								that.bitb = b;
								that.bitk = k;
								z.avail_in = n;
								z.total_in += p - z.next_in_index;
								z.next_in_index = p;
								that.write = q;
								return that.inflate_flush(z, r);
							}

							c = c == 16 ? blens[i - 1] : 0;
							do {
								blens[i++] = c;
							} while (--j !== 0);
							index = i;
						}
					}

					tb[0] = -1;
					// {
					bl_ = []; // new Array(1);
					bd_ = []; // new Array(1);
					tl_ = []; // new Array(1);
					td_ = []; // new Array(1);
					bl_[0] = 9; // must be <= 9 for lookahead assumptions
					bd_[0] = 6; // must be <= 9 for lookahead assumptions

					t = table;
					t = inftree.inflate_trees_dynamic(257 + (t & 0x1f), 1 + ((t >> 5) & 0x1f), blens, bl_, bd_, tl_, td_, hufts, z);

					if (t != Z_OK) {
						if (t == Z_DATA_ERROR) {
							blens = null;
							mode = BADBLOCKS;
						}
						r = t;

						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}
					codes.init(bl_[0], bd_[0], hufts, tl_[0], hufts, td_[0]);
					// }
					mode = CODES;
				/* falls through */
				case CODES:
					that.bitb = b;
					that.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					that.write = q;

					if ((r = codes.proc(that, z, r)) != Z_STREAM_END) {
						return that.inflate_flush(z, r);
					}
					r = Z_OK;
					codes.free(z);

					p = z.next_in_index;
					n = z.avail_in;
					b = that.bitb;
					k = that.bitk;
					q = that.write;
					m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);

					if (last === 0) {
						mode = TYPE;
						break;
					}
					mode = DRY;
				/* falls through */
				case DRY:
					that.write = q;
					r = that.inflate_flush(z, r);
					q = that.write;
					m = /* (int) */(q < that.read ? that.read - q - 1 : that.end - q);
					if (that.read != that.write) {
						that.bitb = b;
						that.bitk = k;
						z.avail_in = n;
						z.total_in += p - z.next_in_index;
						z.next_in_index = p;
						that.write = q;
						return that.inflate_flush(z, r);
					}
					mode = DONELOCKS;
				/* falls through */
				case DONELOCKS:
					r = Z_STREAM_END;

					that.bitb = b;
					that.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					that.write = q;
					return that.inflate_flush(z, r);
				case BADBLOCKS:
					r = Z_DATA_ERROR;

					that.bitb = b;
					that.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					that.write = q;
					return that.inflate_flush(z, r);

				default:
					r = Z_STREAM_ERROR;

					that.bitb = b;
					that.bitk = k;
					z.avail_in = n;
					z.total_in += p - z.next_in_index;
					z.next_in_index = p;
					that.write = q;
					return that.inflate_flush(z, r);
			}
		}
	};

	that.free = function (z) {
		that.reset(z, null);
		that.win = null;
		hufts = null;
		// ZFREE(z, s);
	};

	that.set_dictionary = function (d, start, n) {
		that.win.set(d.subarray(start, start + n), 0);
		that.read = that.write = n;
	};

	// Returns true if inflate is currently at the end of a block generated
	// by Z_SYNC_FLUSH or Z_FULL_FLUSH.
	that.sync_point = function () {
		return mode == LENS ? 1 : 0;
	};

}

// Inflate

// preset dictionary flag in zlib header
const PRESET_DICT = 0x20;

const Z_DEFLATED = 8;

const METHOD = 0; // waiting for method byte
const FLAG = 1; // waiting for flag byte
const DICT4 = 2; // four dictionary check bytes to go
const DICT3 = 3; // three dictionary check bytes to go
const DICT2 = 4; // two dictionary check bytes to go
const DICT1 = 5; // one dictionary check byte to go
const DICT0 = 6; // waiting for inflateSetDictionary
const BLOCKS = 7; // decompressing blocks
const DONE = 12; // finished check, done
const BAD = 13; // got an error--stay here

const mark = [0, 0, 0xff, 0xff];

function Inflate$1() {
	const that = this;

	that.mode = 0; // current inflate mode

	// mode dependent information
	that.method = 0; // if FLAGS, method byte

	// if CHECK, check values to compare
	that.was = [0]; // new Array(1); // computed check value
	that.need = 0; // stream check value

	// if BAD, inflateSync's marker bytes count
	that.marker = 0;

	// mode independent information
	that.wbits = 0; // log2(win size) (8..15, defaults to 15)

	// this.blocks; // current inflate_blocks state

	function inflateReset(z) {
		if (!z || !z.istate)
			return Z_STREAM_ERROR;

		z.total_in = z.total_out = 0;
		z.msg = null;
		z.istate.mode = BLOCKS;
		z.istate.blocks.reset(z, null);
		return Z_OK;
	}

	that.inflateEnd = function (z) {
		if (that.blocks)
			that.blocks.free(z);
		that.blocks = null;
		// ZFREE(z, z->state);
		return Z_OK;
	};

	that.inflateInit = function (z, w) {
		z.msg = null;
		that.blocks = null;

		// set win size
		if (w < 8 || w > 15) {
			that.inflateEnd(z);
			return Z_STREAM_ERROR;
		}
		that.wbits = w;

		z.istate.blocks = new InfBlocks(z, 1 << w);

		// reset state
		inflateReset(z);
		return Z_OK;
	};

	that.inflate = function (z, f) {
		let r;
		let b;

		if (!z || !z.istate || !z.next_in)
			return Z_STREAM_ERROR;
		const istate = z.istate;
		f = f == Z_FINISH ? Z_BUF_ERROR : Z_OK;
		r = Z_BUF_ERROR;
		// eslint-disable-next-line no-constant-condition
		while (true) {
			switch (istate.mode) {
				case METHOD:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					if (((istate.method = z.read_byte(z.next_in_index++)) & 0xf) != Z_DEFLATED) {
						istate.mode = BAD;
						z.msg = "unknown compression method";
						istate.marker = 5; // can't try inflateSync
						break;
					}
					if ((istate.method >> 4) + 8 > istate.wbits) {
						istate.mode = BAD;
						z.msg = "invalid win size";
						istate.marker = 5; // can't try inflateSync
						break;
					}
					istate.mode = FLAG;
				/* falls through */
				case FLAG:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					b = (z.read_byte(z.next_in_index++)) & 0xff;

					if ((((istate.method << 8) + b) % 31) !== 0) {
						istate.mode = BAD;
						z.msg = "incorrect header check";
						istate.marker = 5; // can't try inflateSync
						break;
					}

					if ((b & PRESET_DICT) === 0) {
						istate.mode = BLOCKS;
						break;
					}
					istate.mode = DICT4;
				/* falls through */
				case DICT4:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					istate.need = ((z.read_byte(z.next_in_index++) & 0xff) << 24) & 0xff000000;
					istate.mode = DICT3;
				/* falls through */
				case DICT3:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					istate.need += ((z.read_byte(z.next_in_index++) & 0xff) << 16) & 0xff0000;
					istate.mode = DICT2;
				/* falls through */
				case DICT2:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					istate.need += ((z.read_byte(z.next_in_index++) & 0xff) << 8) & 0xff00;
					istate.mode = DICT1;
				/* falls through */
				case DICT1:

					if (z.avail_in === 0)
						return r;
					r = f;

					z.avail_in--;
					z.total_in++;
					istate.need += (z.read_byte(z.next_in_index++) & 0xff);
					istate.mode = DICT0;
					return Z_NEED_DICT;
				case DICT0:
					istate.mode = BAD;
					z.msg = "need dictionary";
					istate.marker = 0; // can try inflateSync
					return Z_STREAM_ERROR;
				case BLOCKS:

					r = istate.blocks.proc(z, r);
					if (r == Z_DATA_ERROR) {
						istate.mode = BAD;
						istate.marker = 0; // can try inflateSync
						break;
					}
					if (r == Z_OK) {
						r = f;
					}
					if (r != Z_STREAM_END) {
						return r;
					}
					r = f;
					istate.blocks.reset(z, istate.was);
					istate.mode = DONE;
				/* falls through */
				case DONE:
					z.avail_in = 0;
					return Z_STREAM_END;
				case BAD:
					return Z_DATA_ERROR;
				default:
					return Z_STREAM_ERROR;
			}
		}
	};

	that.inflateSetDictionary = function (z, dictionary, dictLength) {
		let index = 0, length = dictLength;
		if (!z || !z.istate || z.istate.mode != DICT0)
			return Z_STREAM_ERROR;
		const istate = z.istate;
		if (length >= (1 << istate.wbits)) {
			length = (1 << istate.wbits) - 1;
			index = dictLength - length;
		}
		istate.blocks.set_dictionary(dictionary, index, length);
		istate.mode = BLOCKS;
		return Z_OK;
	};

	that.inflateSync = function (z) {
		let n; // number of bytes to look at
		let p; // pointer to bytes
		let m; // number of marker bytes found in a row
		let r, w; // temporaries to save total_in and total_out

		// set up
		if (!z || !z.istate)
			return Z_STREAM_ERROR;
		const istate = z.istate;
		if (istate.mode != BAD) {
			istate.mode = BAD;
			istate.marker = 0;
		}
		if ((n = z.avail_in) === 0)
			return Z_BUF_ERROR;
		p = z.next_in_index;
		m = istate.marker;

		// search
		while (n !== 0 && m < 4) {
			if (z.read_byte(p) == mark[m]) {
				m++;
			} else if (z.read_byte(p) !== 0) {
				m = 0;
			} else {
				m = 4 - m;
			}
			p++;
			n--;
		}

		// restore
		z.total_in += p - z.next_in_index;
		z.next_in_index = p;
		z.avail_in = n;
		istate.marker = m;

		// return no joy or set up to restart on a new block
		if (m != 4) {
			return Z_DATA_ERROR;
		}
		r = z.total_in;
		w = z.total_out;
		inflateReset(z);
		z.total_in = r;
		z.total_out = w;
		istate.mode = BLOCKS;
		return Z_OK;
	};

	// Returns true if inflate is currently at the end of a block generated
	// by Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
	// implementation to provide an additional safety check. PPP uses
	// Z_SYNC_FLUSH
	// but removes the length bytes of the resulting empty stored block. When
	// decompressing, PPP checks that at the end of input packet, inflate is
	// waiting for these length bytes.
	that.inflateSyncPoint = function (z) {
		if (!z || !z.istate || !z.istate.blocks)
			return Z_STREAM_ERROR;
		return z.istate.blocks.sync_point();
	};
}

// ZStream

function ZStream() {
}

ZStream.prototype = {
	inflateInit: function (bits) {
		const that = this;
		that.istate = new Inflate$1();
		if (!bits)
			bits = MAX_BITS;
		return that.istate.inflateInit(that, bits);
	},

	inflate: function (f) {
		const that = this;
		if (!that.istate)
			return Z_STREAM_ERROR;
		return that.istate.inflate(that, f);
	},

	inflateEnd: function () {
		const that = this;
		if (!that.istate)
			return Z_STREAM_ERROR;
		const ret = that.istate.inflateEnd(that);
		that.istate = null;
		return ret;
	},

	inflateSync: function () {
		const that = this;
		if (!that.istate)
			return Z_STREAM_ERROR;
		return that.istate.inflateSync(that);
	},
	inflateSetDictionary: function (dictionary, dictLength) {
		const that = this;
		if (!that.istate)
			return Z_STREAM_ERROR;
		return that.istate.inflateSetDictionary(that, dictionary, dictLength);
	},
	read_byte: function (start) {
		const that = this;
		return that.next_in[start];
	},
	read_buf: function (start, size) {
		const that = this;
		return that.next_in.subarray(start, start + size);
	}
};

// Inflater

function ZipInflate(options) {
	const that = this;
	const z = new ZStream();
	const bufsize = options && options.chunkSize ? Math.floor(options.chunkSize * 2) : 128 * 1024;
	const flush = Z_NO_FLUSH;
	const buf = new Uint8Array(bufsize);
	let nomoreinput = false;

	z.inflateInit();
	z.next_out = buf;

	that.append = function (data, onprogress) {
		const buffers = [];
		let err, array, lastIndex = 0, bufferIndex = 0, bufferSize = 0;
		if (data.length === 0)
			return;
		z.next_in_index = 0;
		z.next_in = data;
		z.avail_in = data.length;
		do {
			z.next_out_index = 0;
			z.avail_out = bufsize;
			if ((z.avail_in === 0) && (!nomoreinput)) { // if buffer is empty and more input is available, refill it
				z.next_in_index = 0;
				nomoreinput = true;
			}
			err = z.inflate(flush);
			if (nomoreinput && (err === Z_BUF_ERROR)) {
				if (z.avail_in !== 0)
					throw new Error("inflating: bad input");
			} else if (err !== Z_OK && err !== Z_STREAM_END)
				throw new Error("inflating: " + z.msg);
			if ((nomoreinput || err === Z_STREAM_END) && (z.avail_in === data.length))
				throw new Error("inflating: bad input");
			if (z.next_out_index)
				if (z.next_out_index === bufsize)
					buffers.push(new Uint8Array(buf));
				else
					buffers.push(buf.slice(0, z.next_out_index));
			bufferSize += z.next_out_index;
			if (onprogress && z.next_in_index > 0 && z.next_in_index != lastIndex) {
				onprogress(z.next_in_index);
				lastIndex = z.next_in_index;
			}
		} while (z.avail_in > 0 || z.avail_out === 0);
		if (buffers.length > 1) {
			array = new Uint8Array(bufferSize);
			buffers.forEach(function (chunk) {
				array.set(chunk, bufferIndex);
				bufferIndex += chunk.length;
			});
		} else {
			array = buffers[0] || new Uint8Array(0);
		}
		return array;
	};
	that.flush = function () {
		z.inflateEnd();
	};
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* global navigator */

const DEFAULT_CONFIGURATION = {
	chunkSize: 512 * 1024,
	maxWorkers: (typeof navigator != "undefined" && navigator.hardwareConcurrency) || 2,
	terminateWorkerTimeout: 5000,
	useWebWorkers: true,
	workerScripts: undefined
};

const config = Object.assign({}, DEFAULT_CONFIGURATION);

function getConfiguration() {
	return config;
}

function configure(configuration) {
	if (configuration.baseURL !== undefined) {
		config.baseURL = configuration.baseURL;
	}
	if (configuration.chunkSize !== undefined) {
		config.chunkSize = configuration.chunkSize;
	}
	if (configuration.maxWorkers !== undefined) {
		config.maxWorkers = configuration.maxWorkers;
	}
	if (configuration.terminateWorkerTimeout !== undefined) {
		config.terminateWorkerTimeout = configuration.terminateWorkerTimeout;
	}
	if (configuration.useWebWorkers !== undefined) {
		config.useWebWorkers = configuration.useWebWorkers;
	}
	if (configuration.Deflate !== undefined) {
		config.Deflate = configuration.Deflate;
	}
	if (configuration.Inflate !== undefined) {
		config.Inflate = configuration.Inflate;
	}
	if (configuration.workerScripts !== undefined) {
		if (configuration.workerScripts.deflate) {
			if (!Array.isArray(configuration.workerScripts.deflate)) {
				throw new Error("workerScripts.deflate must be an array");
			}
			if (!config.workerScripts) {
				config.workerScripts = {};
			}
			config.workerScripts.deflate = configuration.workerScripts.deflate;
		}
		if (configuration.workerScripts.inflate) {
			if (!Array.isArray(configuration.workerScripts.inflate)) {
				throw new Error("workerScripts.inflate must be an array");
			}
			if (!config.workerScripts) {
				config.workerScripts = {};
			}
			config.workerScripts.inflate = configuration.workerScripts.inflate;
		}
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

function getMimeType() {
	return "application/octet-stream";
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const table = [];
for (let i = 0; i < 256; i++) {
	let t = i;
	for (let j = 0; j < 8; j++) {
		if (t & 1) {
			t = (t >>> 1) ^ 0xEDB88320;
		} else {
			t = t >>> 1;
		}
	}
	table[i] = t;
}

class Crc32 {

	constructor(crc) {
		this.crc = crc || -1;
	}

	append(data) {
		let crc = this.crc | 0;
		for (let offset = 0, length = data.length | 0; offset < length; offset++) {
			crc = (crc >>> 8) ^ table[(crc ^ data[offset]) & 0xFF];
		}
		this.crc = crc;
	}

	get() {
		return ~this.crc;
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

function encodeText(value) {
	if (typeof TextEncoder == "undefined") {
		value = unescape(encodeURIComponent(value));
		const result = new Uint8Array(value.length);
		for (let i = 0; i < result.length; i++) {
			result[i] = value.charCodeAt(i);
		}
		return result;
	} else {
		return new TextEncoder().encode(value);
	}
}

// Derived from https://github.com/xqdoo00o/jszip/blob/master/lib/sjcl.js and https://github.com/bitwiseshiftleft/sjcl

/*
 * SJCL is open. You can use, modify and redistribute it under a BSD
 * license or under the GNU GPL, version 2.0.
 */

/** @fileOverview Javascript cryptography implementation.
 *
 * Crush to remove comments, shorten variable names and
 * generally reduce transmission size.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

/*jslint indent: 2, bitwise: false, nomen: false, plusplus: false, white: false, regexp: false */

/** @fileOverview Arrays of bits, encoded as arrays of Numbers.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

/**
 * Arrays of bits, encoded as arrays of Numbers.
 * @namespace
 * @description
 * <p>
 * These objects are the currency accepted by SJCL's crypto functions.
 * </p>
 *
 * <p>
 * Most of our crypto primitives operate on arrays of 4-byte words internally,
 * but many of them can take arguments that are not a multiple of 4 bytes.
 * This library encodes arrays of bits (whose size need not be a multiple of 8
 * bits) as arrays of 32-bit words.  The bits are packed, big-endian, into an
 * array of words, 32 bits at a time.  Since the words are double-precision
 * floating point numbers, they fit some extra data.  We use this (in a private,
 * possibly-changing manner) to encode the number of bits actually  present
 * in the last word of the array.
 * </p>
 *
 * <p>
 * Because bitwise ops clear this out-of-band data, these arrays can be passed
 * to ciphers like AES which want arrays of words.
 * </p>
 */
const bitArray = {
	/**
	 * Concatenate two bit arrays.
	 * @param {bitArray} a1 The first array.
	 * @param {bitArray} a2 The second array.
	 * @return {bitArray} The concatenation of a1 and a2.
	 */
	concat(a1, a2) {
		if (a1.length === 0 || a2.length === 0) {
			return a1.concat(a2);
		}

		const last = a1[a1.length - 1], shift = bitArray.getPartial(last);
		if (shift === 32) {
			return a1.concat(a2);
		} else {
			return bitArray._shiftRight(a2, shift, last | 0, a1.slice(0, a1.length - 1));
		}
	},

	/**
	 * Find the length of an array of bits.
	 * @param {bitArray} a The array.
	 * @return {Number} The length of a, in bits.
	 */
	bitLength(a) {
		const l = a.length;
		if (l === 0) {
			return 0;
		}
		const x = a[l - 1];
		return (l - 1) * 32 + bitArray.getPartial(x);
	},

	/**
	 * Truncate an array.
	 * @param {bitArray} a The array.
	 * @param {Number} len The length to truncate to, in bits.
	 * @return {bitArray} A new array, truncated to len bits.
	 */
	clamp(a, len) {
		if (a.length * 32 < len) {
			return a;
		}
		a = a.slice(0, Math.ceil(len / 32));
		const l = a.length;
		len = len & 31;
		if (l > 0 && len) {
			a[l - 1] = bitArray.partial(len, a[l - 1] & 0x80000000 >> (len - 1), 1);
		}
		return a;
	},

	/**
	 * Make a partial word for a bit array.
	 * @param {Number} len The number of bits in the word.
	 * @param {Number} x The bits.
	 * @param {Number} [_end=0] Pass 1 if x has already been shifted to the high side.
	 * @return {Number} The partial word.
	 */
	partial(len, x, _end) {
		if (len === 32) {
			return x;
		}
		return (_end ? x | 0 : x << (32 - len)) + len * 0x10000000000;
	},

	/**
	 * Get the number of bits used by a partial word.
	 * @param {Number} x The partial word.
	 * @return {Number} The number of bits used by the partial word.
	 */
	getPartial(x) {
		return Math.round(x / 0x10000000000) || 32;
	},

	/** Shift an array right.
	 * @param {bitArray} a The array to shift.
	 * @param {Number} shift The number of bits to shift.
	 * @param {Number} [carry=0] A byte to carry in
	 * @param {bitArray} [out=[]] An array to prepend to the output.
	 * @private
	 */
	_shiftRight(a, shift, carry, out) {
		if (out === undefined) {
			out = [];
		}

		for (; shift >= 32; shift -= 32) {
			out.push(carry);
			carry = 0;
		}
		if (shift === 0) {
			return out.concat(a);
		}

		for (let i = 0; i < a.length; i++) {
			out.push(carry | a[i] >>> shift);
			carry = a[i] << (32 - shift);
		}
		const last2 = a.length ? a[a.length - 1] : 0;
		const shift2 = bitArray.getPartial(last2);
		out.push(bitArray.partial(shift + shift2 & 31, (shift + shift2 > 32) ? carry : out.pop(), 1));
		return out;
	}
};

/** @fileOverview Bit array codec implementations.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

/**
 * Arrays of bytes
 * @namespace
 */
const codec = {
	bytes: {
		/** Convert from a bitArray to an array of bytes. */
		fromBits(arr) {
			const bl = bitArray.bitLength(arr);
			const byteLength = bl / 8;
			const out = new Uint8Array(byteLength);
			let tmp;
			for (let i = 0; i < byteLength; i++) {
				if ((i & 3) === 0) {
					tmp = arr[i / 4];
				}
				out[i] = tmp >>> 24;
				tmp <<= 8;
			}
			return out;
		},
		/** Convert from an array of bytes to a bitArray. */
		toBits(bytes) {
			const out = [];
			let i;
			let tmp = 0;
			for (i = 0; i < bytes.length; i++) {
				tmp = tmp << 8 | bytes[i];
				if ((i & 3) === 3) {
					out.push(tmp);
					tmp = 0;
				}
			}
			if (i & 3) {
				out.push(bitArray.partial(8 * (i & 3), tmp));
			}
			return out;
		}
	}
};

const hash = {};

/**
 * Context for a SHA-1 operation in progress.
 * @constructor
 */
hash.sha1 = function (hash) {
	if (hash) {
		this._h = hash._h.slice(0);
		this._buffer = hash._buffer.slice(0);
		this._length = hash._length;
	} else {
		this.reset();
	}
};

hash.sha1.prototype = {
	/**
	 * The hash's block size, in bits.
	 * @constant
	 */
	blockSize: 512,

	/**
	 * Reset the hash state.
	 * @return this
	 */
	reset: function () {
		const sha1 = this;
		sha1._h = this._init.slice(0);
		sha1._buffer = [];
		sha1._length = 0;
		return sha1;
	},

	/**
	 * Input several words to the hash.
	 * @param {bitArray|String} data the data to hash.
	 * @return this
	 */
	update: function (data) {
		const sha1 = this;
		if (typeof data === "string") {
			data = codec.utf8String.toBits(data);
		}
		const b = sha1._buffer = bitArray.concat(sha1._buffer, data);
		const ol = sha1._length;
		const nl = sha1._length = ol + bitArray.bitLength(data);
		if (nl > 9007199254740991) {
			throw new Error("Cannot hash more than 2^53 - 1 bits");
		}
		const c = new Uint32Array(b);
		let j = 0;
		for (let i = sha1.blockSize + ol - ((sha1.blockSize + ol) & (sha1.blockSize - 1)); i <= nl;
			i += sha1.blockSize) {
			sha1._block(c.subarray(16 * j, 16 * (j + 1)));
			j += 1;
		}
		b.splice(0, 16 * j);
		return sha1;
	},

	/**
	 * Complete hashing and output the hash value.
	 * @return {bitArray} The hash value, an array of 5 big-endian words. TODO
	 */
	finalize: function () {
		const sha1 = this;
		let b = sha1._buffer;
		const h = sha1._h;

		// Round out and push the buffer
		b = bitArray.concat(b, [bitArray.partial(1, 1)]);
		// Round out the buffer to a multiple of 16 words, less the 2 length words.
		for (let i = b.length + 2; i & 15; i++) {
			b.push(0);
		}

		// append the length
		b.push(Math.floor(sha1._length / 0x100000000));
		b.push(sha1._length | 0);

		while (b.length) {
			sha1._block(b.splice(0, 16));
		}

		sha1.reset();
		return h;
	},

	/**
	 * The SHA-1 initialization vector.
	 * @private
	 */
	_init: [0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0],

	/**
	 * The SHA-1 hash key.
	 * @private
	 */
	_key: [0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xCA62C1D6],

	/**
	 * The SHA-1 logical functions f(0), f(1), ..., f(79).
	 * @private
	 */
	_f: function (t, b, c, d) {
		if (t <= 19) {
			return (b & c) | (~b & d);
		} else if (t <= 39) {
			return b ^ c ^ d;
		} else if (t <= 59) {
			return (b & c) | (b & d) | (c & d);
		} else if (t <= 79) {
			return b ^ c ^ d;
		}
	},

	/**
	 * Circular left-shift operator.
	 * @private
	 */
	_S: function (n, x) {
		return (x << n) | (x >>> 32 - n);
	},

	/**
	 * Perform one cycle of SHA-1.
	 * @param {Uint32Array|bitArray} words one block of words.
	 * @private
	 */
	_block: function (words) {
		const sha1 = this;
		const h = sha1._h;
		// When words is passed to _block, it has 16 elements. SHA1 _block
		// function extends words with new elements (at the end there are 80 elements). 
		// The problem is that if we use Uint32Array instead of Array, 
		// the length of Uint32Array cannot be changed. Thus, we replace words with a 
		// normal Array here.
		const w = Array(80); // do not use Uint32Array here as the instantiation is slower
		for (let j = 0; j < 16; j++) {
			w[j] = words[j];
		}

		let a = h[0];
		let b = h[1];
		let c = h[2];
		let d = h[3];
		let e = h[4];

		for (let t = 0; t <= 79; t++) {
			if (t >= 16) {
				w[t] = sha1._S(1, w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16]);
			}
			const tmp = (sha1._S(5, a) + sha1._f(t, b, c, d) + e + w[t] +
				sha1._key[Math.floor(t / 20)]) | 0;
			e = d;
			d = c;
			c = sha1._S(30, b);
			b = a;
			a = tmp;
		}

		h[0] = (h[0] + a) | 0;
		h[1] = (h[1] + b) | 0;
		h[2] = (h[2] + c) | 0;
		h[3] = (h[3] + d) | 0;
		h[4] = (h[4] + e) | 0;
	}
};

/** @fileOverview Low-level AES implementation.
 *
 * This file contains a low-level implementation of AES, optimized for
 * size and for efficiency on several browsers.  It is based on
 * OpenSSL's aes_core.c, a public-domain implementation by Vincent
 * Rijmen, Antoon Bosselaers and Paulo Barreto.
 *
 * An older version of this implementation is available in the public
 * domain, but this one is (c) Emily Stark, Mike Hamburg, Dan Boneh,
 * Stanford University 2008-2010 and BSD-licensed for liability
 * reasons.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

const cipher = {};

/**
 * Schedule out an AES key for both encryption and decryption.  This
 * is a low-level class.  Use a cipher mode to do bulk encryption.
 *
 * @constructor
 * @param {Array} key The key as an array of 4, 6 or 8 words.
 */
cipher.aes = class {
	constructor(key) {
		/**
		 * The expanded S-box and inverse S-box tables.  These will be computed
		 * on the client so that we don't have to send them down the wire.
		 *
		 * There are two tables, _tables[0] is for encryption and
		 * _tables[1] is for decryption.
		 *
		 * The first 4 sub-tables are the expanded S-box with MixColumns.  The
		 * last (_tables[01][4]) is the S-box itself.
		 *
		 * @private
		 */
		const aes = this;
		aes._tables = [[[], [], [], [], []], [[], [], [], [], []]];

		if (!aes._tables[0][0][0]) {
			aes._precompute();
		}

		const sbox = aes._tables[0][4];
		const decTable = aes._tables[1];
		const keyLen = key.length;

		let i, encKey, decKey, rcon = 1;

		if (keyLen !== 4 && keyLen !== 6 && keyLen !== 8) {
			throw new Error("invalid aes key size");
		}

		aes._key = [encKey = key.slice(0), decKey = []];

		// schedule encryption keys
		for (i = keyLen; i < 4 * keyLen + 28; i++) {
			let tmp = encKey[i - 1];

			// apply sbox
			if (i % keyLen === 0 || (keyLen === 8 && i % keyLen === 4)) {
				tmp = sbox[tmp >>> 24] << 24 ^ sbox[tmp >> 16 & 255] << 16 ^ sbox[tmp >> 8 & 255] << 8 ^ sbox[tmp & 255];

				// shift rows and add rcon
				if (i % keyLen === 0) {
					tmp = tmp << 8 ^ tmp >>> 24 ^ rcon << 24;
					rcon = rcon << 1 ^ (rcon >> 7) * 283;
				}
			}

			encKey[i] = encKey[i - keyLen] ^ tmp;
		}

		// schedule decryption keys
		for (let j = 0; i; j++, i--) {
			const tmp = encKey[j & 3 ? i : i - 4];
			if (i <= 4 || j < 4) {
				decKey[j] = tmp;
			} else {
				decKey[j] = decTable[0][sbox[tmp >>> 24]] ^
					decTable[1][sbox[tmp >> 16 & 255]] ^
					decTable[2][sbox[tmp >> 8 & 255]] ^
					decTable[3][sbox[tmp & 255]];
			}
		}
	}
	// public
	/* Something like this might appear here eventually
	name: "AES",
	blockSize: 4,
	keySizes: [4,6,8],
	*/

	/**
	 * Encrypt an array of 4 big-endian words.
	 * @param {Array} data The plaintext.
	 * @return {Array} The ciphertext.
	 */
	encrypt(data) {
		return this._crypt(data, 0);
	}

	/**
	 * Decrypt an array of 4 big-endian words.
	 * @param {Array} data The ciphertext.
	 * @return {Array} The plaintext.
	 */
	decrypt(data) {
		return this._crypt(data, 1);
	}

	/**
	 * Expand the S-box tables.
	 *
	 * @private
	 */
	_precompute() {
		const encTable = this._tables[0];
		const decTable = this._tables[1];
		const sbox = encTable[4];
		const sboxInv = decTable[4];
		const d = [];
		const th = [];
		let xInv, x2, x4, x8;

		// Compute double and third tables
		for (let i = 0; i < 256; i++) {
			th[(d[i] = i << 1 ^ (i >> 7) * 283) ^ i] = i;
		}

		for (let x = xInv = 0; !sbox[x]; x ^= x2 || 1, xInv = th[xInv] || 1) {
			// Compute sbox
			let s = xInv ^ xInv << 1 ^ xInv << 2 ^ xInv << 3 ^ xInv << 4;
			s = s >> 8 ^ s & 255 ^ 99;
			sbox[x] = s;
			sboxInv[s] = x;

			// Compute MixColumns
			x8 = d[x4 = d[x2 = d[x]]];
			let tDec = x8 * 0x1010101 ^ x4 * 0x10001 ^ x2 * 0x101 ^ x * 0x1010100;
			let tEnc = d[s] * 0x101 ^ s * 0x1010100;

			for (let i = 0; i < 4; i++) {
				encTable[i][x] = tEnc = tEnc << 24 ^ tEnc >>> 8;
				decTable[i][s] = tDec = tDec << 24 ^ tDec >>> 8;
			}
		}

		// Compactify.  Considerable speedup on Firefox.
		for (let i = 0; i < 5; i++) {
			encTable[i] = encTable[i].slice(0);
			decTable[i] = decTable[i].slice(0);
		}
	}

	/**
	 * Encryption and decryption core.
	 * @param {Array} input Four words to be encrypted or decrypted.
	 * @param dir The direction, 0 for encrypt and 1 for decrypt.
	 * @return {Array} The four encrypted or decrypted words.
	 * @private
	 */
	_crypt(input, dir) {
		if (input.length !== 4) {
			throw new Error("invalid aes block size");
		}

		const key = this._key[dir];

		const nInnerRounds = key.length / 4 - 2;
		const out = [0, 0, 0, 0];
		const table = this._tables[dir];

		// load up the tables
		const t0 = table[0];
		const t1 = table[1];
		const t2 = table[2];
		const t3 = table[3];
		const sbox = table[4];

		// state variables a,b,c,d are loaded with pre-whitened data
		let a = input[0] ^ key[0];
		let b = input[dir ? 3 : 1] ^ key[1];
		let c = input[2] ^ key[2];
		let d = input[dir ? 1 : 3] ^ key[3];
		let kIndex = 4;
		let a2, b2, c2;

		// Inner rounds.  Cribbed from OpenSSL.
		for (let i = 0; i < nInnerRounds; i++) {
			a2 = t0[a >>> 24] ^ t1[b >> 16 & 255] ^ t2[c >> 8 & 255] ^ t3[d & 255] ^ key[kIndex];
			b2 = t0[b >>> 24] ^ t1[c >> 16 & 255] ^ t2[d >> 8 & 255] ^ t3[a & 255] ^ key[kIndex + 1];
			c2 = t0[c >>> 24] ^ t1[d >> 16 & 255] ^ t2[a >> 8 & 255] ^ t3[b & 255] ^ key[kIndex + 2];
			d = t0[d >>> 24] ^ t1[a >> 16 & 255] ^ t2[b >> 8 & 255] ^ t3[c & 255] ^ key[kIndex + 3];
			kIndex += 4;
			a = a2; b = b2; c = c2;
		}

		// Last round.
		for (let i = 0; i < 4; i++) {
			out[dir ? 3 & -i : i] =
				sbox[a >>> 24] << 24 ^
				sbox[b >> 16 & 255] << 16 ^
				sbox[c >> 8 & 255] << 8 ^
				sbox[d & 255] ^
				key[kIndex++];
			a2 = a; a = b; b = c; c = d; d = a2;
		}

		return out;
	}
};

/**
 * Random values
 * @namespace
 */
const random = {
	/** 
	 * Generate random words with pure js, cryptographically not as strong & safe as native implementation.
	 * @param {TypedArray} typedArray The array to fill.
	 * @return {TypedArray} The random values.
	 */
	getRandomValues(typedArray) {
		const words = new Uint32Array(typedArray.buffer);
		const r = (m_w) => {
			let m_z = 0x3ade68b1;
			const mask = 0xffffffff;
			return function () {
				m_z = (0x9069 * (m_z & 0xFFFF) + (m_z >> 0x10)) & mask;
				m_w = (0x4650 * (m_w & 0xFFFF) + (m_w >> 0x10)) & mask;
				const result = ((((m_z << 0x10) + m_w) & mask) / 0x100000000) + .5;
				return result * (Math.random() > .5 ? 1 : -1);
			};
		};
		for (let i = 0, rcache; i < typedArray.length; i += 4) {
			let _r = r((rcache || Math.random()) * 0x100000000);
			rcache = _r() * 0x3ade67b7;
			words[i / 4] = (_r() * 0x100000000) | 0;
		}
		return typedArray;
	}
};

/** @fileOverview CTR mode implementation.
 *
 * Special thanks to Roy Nicholson for pointing out a bug in our
 * implementation.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

/** Brian Gladman's CTR Mode.
* @constructor
* @param {Object} _prf The aes instance to generate key.
* @param {bitArray} _iv The iv for ctr mode, it must be 128 bits.
*/

const mode = {};

/**
 * Brian Gladman's CTR Mode.
 * @namespace
 */
mode.ctrGladman = class {
	constructor(prf, iv) {
		this._prf = prf;
		this._initIv = iv;
		this._iv = iv;
	}

	reset() {
		this._iv = this._initIv;
	}

	/** Input some data to calculate.
	 * @param {bitArray} data the data to process, it must be intergral multiple of 128 bits unless it's the last.
	 */
	update(data) {
		return this.calculate(this._prf, data, this._iv);
	}

	incWord(word) {
		if (((word >> 24) & 0xff) === 0xff) { //overflow
			let b1 = (word >> 16) & 0xff;
			let b2 = (word >> 8) & 0xff;
			let b3 = word & 0xff;

			if (b1 === 0xff) { // overflow b1   
				b1 = 0;
				if (b2 === 0xff) {
					b2 = 0;
					if (b3 === 0xff) {
						b3 = 0;
					} else {
						++b3;
					}
				} else {
					++b2;
				}
			} else {
				++b1;
			}

			word = 0;
			word += (b1 << 16);
			word += (b2 << 8);
			word += b3;
		} else {
			word += (0x01 << 24);
		}
		return word;
	}

	incCounter(counter) {
		if ((counter[0] = this.incWord(counter[0])) === 0) {
			// encr_data in fileenc.c from  Dr Brian Gladman's counts only with DWORD j < 8
			counter[1] = this.incWord(counter[1]);
		}
	}

	calculate(prf, data, iv) {
		let l;
		if (!(l = data.length)) {
			return [];
		}
		const bl = bitArray.bitLength(data);
		for (let i = 0; i < l; i += 4) {
			this.incCounter(iv);
			const e = prf.encrypt(iv);
			data[i] ^= e[0];
			data[i + 1] ^= e[1];
			data[i + 2] ^= e[2];
			data[i + 3] ^= e[3];
		}
		return bitArray.clamp(data, bl);
	}
};

const misc = {
	importKey(password) {
		return new misc.hmacSha1(codec.bytes.toBits(password));
	},
	pbkdf2(prf, salt, count, length) {
		count = count || 10000;
		if (length < 0 || count < 0) {
			throw new Error("invalid params to pbkdf2");
		}
		const byteLength = ((length >> 5) + 1) << 2;
		let u, ui, i, j, k;
		const arrayBuffer = new ArrayBuffer(byteLength);
		let out = new DataView(arrayBuffer);
		let outLength = 0;
		const b = bitArray;
		salt = codec.bytes.toBits(salt);
		for (k = 1; outLength < (byteLength || 1); k++) {
			u = ui = prf.encrypt(b.concat(salt, [k]));
			for (i = 1; i < count; i++) {
				ui = prf.encrypt(ui);
				for (j = 0; j < ui.length; j++) {
					u[j] ^= ui[j];
				}
			}
			for (i = 0; outLength < (byteLength || 1) && i < u.length; i++) {
				out.setInt32(outLength, u[i]);
				outLength += 4;
			}
		}
		return arrayBuffer.slice(0, length / 8);
	}
};

/** @fileOverview HMAC implementation.
 *
 * @author Emily Stark
 * @author Mike Hamburg
 * @author Dan Boneh
 */

/** HMAC with the specified hash function.
 * @constructor
 * @param {bitArray} key the key for HMAC.
 * @param {Object} [Hash=hash.sha1] The hash function to use.
 */
misc.hmacSha1 = class {

	constructor(key) {
		const hmac = this;
		const Hash = hmac._hash = hash.sha1;
		const exKey = [[], []];
		const bs = Hash.prototype.blockSize / 32;
		hmac._baseHash = [new Hash(), new Hash()];

		if (key.length > bs) {
			key = Hash.hash(key);
		}

		for (let i = 0; i < bs; i++) {
			exKey[0][i] = key[i] ^ 0x36363636;
			exKey[1][i] = key[i] ^ 0x5C5C5C5C;
		}

		hmac._baseHash[0].update(exKey[0]);
		hmac._baseHash[1].update(exKey[1]);
		hmac._resultHash = new Hash(hmac._baseHash[0]);
	}
	reset() {
		const hmac = this;
		hmac._resultHash = new hmac._hash(hmac._baseHash[0]);
		hmac._updated = false;
	}

	update(data) {
		const hmac = this;
		hmac._updated = true;
		hmac._resultHash.update(data);
	}

	digest() {
		const hmac = this;
		const w = hmac._resultHash.finalize();
		const result = new (hmac._hash)(hmac._baseHash[1]).update(w).finalize();

		hmac.reset();

		return result;
	}

	encrypt(data) {
		if (!this._updated) {
			this.update(data);
			return this.digest(data);
		} else {
			throw new Error("encrypt on already updated hmac called!");
		}
	}
};

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const ERR_INVALID_PASSWORD = "Invalid pasword";
const BLOCK_LENGTH = 16;
const RAW_FORMAT = "raw";
const PBKDF2_ALGORITHM = { name: "PBKDF2" };
const HASH_ALGORITHM = { name: "HMAC" };
const HASH_FUNCTION = "SHA-1";
const BASE_KEY_ALGORITHM = Object.assign({ hash: HASH_ALGORITHM }, PBKDF2_ALGORITHM);
const DERIVED_BITS_ALGORITHM = Object.assign({ iterations: 1000, hash: { name: HASH_FUNCTION } }, PBKDF2_ALGORITHM);
const DERIVED_BITS_USAGE = ["deriveBits"];
const SALT_LENGTH = [8, 12, 16];
const KEY_LENGTH = [16, 24, 32];
const SIGNATURE_LENGTH = 10;
const COUNTER_DEFAULT_VALUE = [0, 0, 0, 0];
const CRYPTO_API_SUPPORTED = typeof crypto != "undefined";
const SUBTLE_API_SUPPORTED = CRYPTO_API_SUPPORTED && typeof crypto.subtle != "undefined";
const codecBytes = codec.bytes;
const Aes = cipher.aes;
const CtrGladman = mode.ctrGladman;
const HmacSha1 = misc.hmacSha1;
class AESDecrypt {

	constructor(password, signed, strength) {
		Object.assign(this, {
			password,
			signed,
			strength: strength - 1,
			pendingInput: new Uint8Array(0)
		});
	}

	async append(input) {
		const aesCrypto = this;
		if (aesCrypto.password) {
			const preamble = subarray(input, 0, SALT_LENGTH[aesCrypto.strength] + 2);
			await createDecryptionKeys(aesCrypto, preamble, aesCrypto.password);
			aesCrypto.password = null;
			aesCrypto.aesCtrGladman = new CtrGladman(new Aes(aesCrypto.keys.key), Array.from(COUNTER_DEFAULT_VALUE));
			aesCrypto.hmac = new HmacSha1(aesCrypto.keys.authentication);
			input = subarray(input, SALT_LENGTH[aesCrypto.strength] + 2);
		}
		const output = new Uint8Array(input.length - SIGNATURE_LENGTH - ((input.length - SIGNATURE_LENGTH) % BLOCK_LENGTH));
		return append(aesCrypto, input, output, 0, SIGNATURE_LENGTH, true);
	}

	flush() {
		const aesCrypto = this;
		const pendingInput = aesCrypto.pendingInput;
		const chunkToDecrypt = subarray(pendingInput, 0, pendingInput.length - SIGNATURE_LENGTH);
		const originalSignature = subarray(pendingInput, pendingInput.length - SIGNATURE_LENGTH);
		let decryptedChunkArray = new Uint8Array(0);
		if (chunkToDecrypt.length) {
			const encryptedChunk = codecBytes.toBits(chunkToDecrypt);
			aesCrypto.hmac.update(encryptedChunk);
			const decryptedChunk = aesCrypto.aesCtrGladman.update(encryptedChunk);
			decryptedChunkArray = codecBytes.fromBits(decryptedChunk);
		}
		let valid = true;
		if (aesCrypto.signed) {
			const signature = subarray(codecBytes.fromBits(aesCrypto.hmac.digest()), 0, SIGNATURE_LENGTH);
			for (let indexSignature = 0; indexSignature < SIGNATURE_LENGTH; indexSignature++) {
				if (signature[indexSignature] != originalSignature[indexSignature]) {
					valid = false;
				}
			}
		}
		return {
			valid,
			data: decryptedChunkArray
		};
	}
}

class AESEncrypt {

	constructor(password, strength) {
		Object.assign(this, {
			password,
			strength: strength - 1,
			pendingInput: new Uint8Array(0)
		});
	}

	async append(input) {
		const aesCrypto = this;
		let preamble = new Uint8Array(0);
		if (aesCrypto.password) {
			preamble = await createEncryptionKeys(aesCrypto, aesCrypto.password);
			aesCrypto.password = null;
			aesCrypto.aesCtrGladman = new CtrGladman(new Aes(aesCrypto.keys.key), Array.from(COUNTER_DEFAULT_VALUE));
			aesCrypto.hmac = new HmacSha1(aesCrypto.keys.authentication);
		}
		const output = new Uint8Array(preamble.length + input.length - (input.length % BLOCK_LENGTH));
		output.set(preamble, 0);
		return append(aesCrypto, input, output, preamble.length, 0);
	}

	flush() {
		const aesCrypto = this;
		let encryptedChunkArray = new Uint8Array(0);
		if (aesCrypto.pendingInput.length) {
			const encryptedChunk = aesCrypto.aesCtrGladman.update(codecBytes.toBits(aesCrypto.pendingInput));
			aesCrypto.hmac.update(encryptedChunk);
			encryptedChunkArray = codecBytes.fromBits(encryptedChunk);
		}
		const signature = subarray(codecBytes.fromBits(aesCrypto.hmac.digest()), 0, SIGNATURE_LENGTH);
		return {
			data: concat(encryptedChunkArray, signature),
			signature
		};
	}
}

function append(aesCrypto, input, output, paddingStart, paddingEnd, verifySignature) {
	const inputLength = input.length - paddingEnd;
	if (aesCrypto.pendingInput.length) {
		input = concat(aesCrypto.pendingInput, input);
		output = expand(output, inputLength - (inputLength % BLOCK_LENGTH));
	}
	let offset;
	for (offset = 0; offset <= inputLength - BLOCK_LENGTH; offset += BLOCK_LENGTH) {
		const inputChunk = codecBytes.toBits(subarray(input, offset, offset + BLOCK_LENGTH));
		if (verifySignature) {
			aesCrypto.hmac.update(inputChunk);
		}
		const outputChunk = aesCrypto.aesCtrGladman.update(inputChunk);
		if (!verifySignature) {
			aesCrypto.hmac.update(outputChunk);
		}
		output.set(codecBytes.fromBits(outputChunk), offset + paddingStart);
	}
	aesCrypto.pendingInput = subarray(input, offset);
	return output;
}

async function createDecryptionKeys(decrypt, preambleArray, password) {
	await createKeys$1(decrypt, password, subarray(preambleArray, 0, SALT_LENGTH[decrypt.strength]));
	const passwordVerification = subarray(preambleArray, SALT_LENGTH[decrypt.strength]);
	const passwordVerificationKey = decrypt.keys.passwordVerification;
	if (passwordVerificationKey[0] != passwordVerification[0] || passwordVerificationKey[1] != passwordVerification[1]) {
		throw new Error(ERR_INVALID_PASSWORD);
	}
}

async function createEncryptionKeys(encrypt, password) {
	const salt = getRandomValues(new Uint8Array(SALT_LENGTH[encrypt.strength]));
	await createKeys$1(encrypt, password, salt);
	return concat(salt, encrypt.keys.passwordVerification);
}

async function createKeys$1(target, password, salt) {
	const encodedPassword = encodeText(password);
	const basekey = await importKey(RAW_FORMAT, encodedPassword, BASE_KEY_ALGORITHM, false, DERIVED_BITS_USAGE);
	const derivedBits = await deriveBits(Object.assign({ salt }, DERIVED_BITS_ALGORITHM), basekey, 8 * ((KEY_LENGTH[target.strength] * 2) + 2));
	const compositeKey = new Uint8Array(derivedBits);
	target.keys = {
		key: codecBytes.toBits(subarray(compositeKey, 0, KEY_LENGTH[target.strength])),
		authentication: codecBytes.toBits(subarray(compositeKey, KEY_LENGTH[target.strength], KEY_LENGTH[target.strength] * 2)),
		passwordVerification: subarray(compositeKey, KEY_LENGTH[target.strength] * 2)
	};
}

function getRandomValues(array) {
	if (CRYPTO_API_SUPPORTED && typeof crypto.getRandomValues == "function") {
		return crypto.getRandomValues(array);
	} else {
		return random.getRandomValues(array);
	}
}

async function importKey(format, password, algorithm, extractable, keyUsages) {
	if (CRYPTO_API_SUPPORTED && SUBTLE_API_SUPPORTED && typeof crypto.subtle.importKey == "function") {
		return crypto.subtle.importKey(format, password, algorithm, extractable, keyUsages);
	} else {
		return misc.importKey(password);
	}
}

async function deriveBits(algorithm, baseKey, length) {
	if (CRYPTO_API_SUPPORTED && SUBTLE_API_SUPPORTED && typeof crypto.subtle.deriveBits == "function") {
		return await crypto.subtle.deriveBits(algorithm, baseKey, length);
	} else {
		return misc.pbkdf2(baseKey, algorithm.salt, DERIVED_BITS_ALGORITHM.iterations, length);
	}
}

function concat(leftArray, rightArray) {
	let array = leftArray;
	if (leftArray.length + rightArray.length) {
		array = new Uint8Array(leftArray.length + rightArray.length);
		array.set(leftArray, 0);
		array.set(rightArray, leftArray.length);
	}
	return array;
}

function expand(inputArray, length) {
	if (length && length > inputArray.length) {
		const array = inputArray;
		inputArray = new Uint8Array(length);
		inputArray.set(array, 0);
	}
	return inputArray;
}

function subarray(array, begin, end) {
	return array.subarray(begin, end);
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const HEADER_LENGTH = 12;

class ZipCryptoDecrypt {

	constructor(password, passwordVerification) {
		const zipCrypto = this;
		Object.assign(zipCrypto, {
			password,
			passwordVerification
		});
		createKeys(zipCrypto, password);
	}

	append(input) {
		const zipCrypto = this;
		if (zipCrypto.password) {
			const decryptedHeader = decrypt(zipCrypto, input.subarray(0, HEADER_LENGTH));
			zipCrypto.password = null;
			if (decryptedHeader[HEADER_LENGTH - 1] != zipCrypto.passwordVerification) {
				throw new Error(ERR_INVALID_PASSWORD);
			}
			input = input.subarray(HEADER_LENGTH);
		}
		return decrypt(zipCrypto, input);
	}

	flush() {
		return {
			valid: true,
			data: new Uint8Array(0)
		};
	}
}

class ZipCryptoEncrypt {

	constructor(password, passwordVerification) {
		const zipCrypto = this;
		Object.assign(zipCrypto, {
			password,
			passwordVerification
		});
		createKeys(zipCrypto, password);
	}

	append(input) {
		const zipCrypto = this;
		let output;
		let offset;
		if (zipCrypto.password) {
			zipCrypto.password = null;
			const header = crypto.getRandomValues(new Uint8Array(HEADER_LENGTH));
			header[HEADER_LENGTH - 1] = zipCrypto.passwordVerification;
			output = new Uint8Array(input.length + header.length);
			output.set(encrypt(zipCrypto, header), 0);
			offset = HEADER_LENGTH;
		} else {
			output = new Uint8Array(input.length);
			offset = 0;
		}
		output.set(encrypt(zipCrypto, input), offset);
		return output;
	}

	flush() {
		return {
			data: new Uint8Array(0)
		};
	}
}

function decrypt(target, input) {
	const output = new Uint8Array(input.length);
	for (let index = 0; index < input.length; index++) {
		output[index] = getByte(target) ^ input[index];
		updateKeys(target, output[index]);
	}
	return output;
}

function encrypt(target, input) {
	const output = new Uint8Array(input.length);
	for (let index = 0; index < input.length; index++) {
		output[index] = getByte(target) ^ input[index];
		updateKeys(target, input[index]);
	}
	return output;
}

function createKeys(target, password) {
	target.keys = [0x12345678, 0x23456789, 0x34567890];
	target.crcKey0 = new Crc32(target.keys[0]);
	target.crcKey2 = new Crc32(target.keys[2]);
	for (let index = 0; index < password.length; index++) {
		updateKeys(target, password.charCodeAt(index));
	}
}

function updateKeys(target, byte) {
	target.crcKey0.append([byte]);
	target.keys[0] = ~target.crcKey0.get();
	target.keys[1] = getInt32(target.keys[1] + getInt8(target.keys[0]));
	target.keys[1] = getInt32(Math.imul(target.keys[1], 134775813) + 1);
	target.crcKey2.append([target.keys[1] >>> 24]);
	target.keys[2] = ~target.crcKey2.get();
}

function getByte(target) {
	const temp = target.keys[2] | 2;
	return getInt8(Math.imul(temp, (temp ^ 1)) >>> 8);
}

function getInt8(number) {
	return number & 0xFF;
}

function getInt32(number) {
	return number & 0xFFFFFFFF;
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const CODEC_DEFLATE = "deflate";
const CODEC_INFLATE = "inflate";
const ERR_INVALID_SIGNATURE = "Invalid signature";

class Inflate {

	constructor(codecConstructor, {
		signature,
		password,
		signed,
		compressed,
		zipCrypto,
		passwordVerification,
		encryptionStrength
	}, { chunkSize }) {
		const encrypted = Boolean(password);
		Object.assign(this, {
			signature,
			encrypted,
			signed,
			compressed,
			inflate: compressed && new codecConstructor({ chunkSize }),
			crc32: signed && new Crc32(),
			zipCrypto,
			decrypt: encrypted && zipCrypto ?
				new ZipCryptoDecrypt(password, passwordVerification) :
				new AESDecrypt(password, signed, encryptionStrength)
		});
	}

	async append(data) {
		const codec = this;
		if (codec.encrypted && data.length) {
			data = await codec.decrypt.append(data);
		}
		if (codec.compressed && data.length) {
			data = await codec.inflate.append(data);
		}
		if ((!codec.encrypted || codec.zipCrypto) && codec.signed && data.length) {
			codec.crc32.append(data);
		}
		return data;
	}

	async flush() {
		const codec = this;
		let signature;
		let data = new Uint8Array(0);
		if (codec.encrypted) {
			const result = codec.decrypt.flush();
			if (!result.valid) {
				throw new Error(ERR_INVALID_SIGNATURE);
			}
			data = result.data;
		}
		if ((!codec.encrypted || codec.zipCrypto) && codec.signed) {
			const dataViewSignature = new DataView(new Uint8Array(4).buffer);
			signature = codec.crc32.get();
			dataViewSignature.setUint32(0, signature);
			if (codec.signature != dataViewSignature.getUint32(0, false)) {
				throw new Error(ERR_INVALID_SIGNATURE);
			}
		}
		if (codec.compressed) {
			data = (await codec.inflate.append(data)) || new Uint8Array(0);
			await codec.inflate.flush();
		}
		return { data, signature };
	}
}

class Deflate {

	constructor(codecConstructor, {
		encrypted,
		signed,
		compressed,
		level,
		zipCrypto,
		password,
		passwordVerification,
		encryptionStrength
	}, { chunkSize }) {
		Object.assign(this, {
			encrypted,
			signed,
			compressed,
			deflate: compressed && new codecConstructor({ level: level || 5, chunkSize }),
			crc32: signed && new Crc32(),
			zipCrypto,
			encrypt: encrypted && zipCrypto ?
				new ZipCryptoEncrypt(password, passwordVerification) :
				new AESEncrypt(password, encryptionStrength)
		});
	}

	async append(inputData) {
		const codec = this;
		let data = inputData;
		if (codec.compressed && inputData.length) {
			data = await codec.deflate.append(inputData);
		}
		if (codec.encrypted && data.length) {
			data = await codec.encrypt.append(data);
		}
		if ((!codec.encrypted || codec.zipCrypto) && codec.signed && inputData.length) {
			codec.crc32.append(inputData);
		}
		return data;
	}

	async flush() {
		const codec = this;
		let signature;
		let data = new Uint8Array(0);
		if (codec.compressed) {
			data = (await codec.deflate.flush()) || new Uint8Array(0);
		}
		if (codec.encrypted) {
			data = await codec.encrypt.append(data);
			const result = codec.encrypt.flush();
			signature = result.signature;
			const newData = new Uint8Array(data.length + result.data.length);
			newData.set(data, 0);
			newData.set(result.data, data.length);
			data = newData;
		}
		if ((!codec.encrypted || codec.zipCrypto) && codec.signed) {
			signature = codec.crc32.get();
		}
		return { data, signature };
	}
}

function createCodec$1(codecConstructor, options, config) {
	if (options.codecType.startsWith(CODEC_DEFLATE)) {
		return new Deflate(codecConstructor, options, config);
	} else if (options.codecType.startsWith(CODEC_INFLATE)) {
		return new Inflate(codecConstructor, options, config);
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const MESSAGE_INIT = "init";
const MESSAGE_APPEND = "append";
const MESSAGE_FLUSH = "flush";
const MESSAGE_EVENT_TYPE = "message";

let classicWorkersSupported = true;

var getWorker = (workerData, codecConstructor, options, config, onTaskFinished, webWorker, scripts) => {
	Object.assign(workerData, {
		busy: true,
		codecConstructor,
		options: Object.assign({}, options),
		scripts,
		terminate() {
			if (workerData.worker && !workerData.busy) {
				workerData.worker.terminate();
				workerData.interface = null;
			}
		},
		onTaskFinished() {
			workerData.busy = false;
			onTaskFinished(workerData);
		}
	});
	return webWorker ? createWebWorkerInterface(workerData, config) : createWorkerInterface(workerData, config);
};

function createWorkerInterface(workerData, config) {
	const interfaceCodec = createCodec$1(workerData.codecConstructor, workerData.options, config);
	return {
		async append(data) {
			try {
				return await interfaceCodec.append(data);
			} catch (error) {
				workerData.onTaskFinished();
				throw error;
			}
		},
		async flush() {
			try {
				return await interfaceCodec.flush();
			} finally {
				workerData.onTaskFinished();
			}
		},
		abort() {
			workerData.onTaskFinished();
		}
	};
}

function createWebWorkerInterface(workerData, config) {
	let messageTask;
	const workerOptions = { type: "module" };
	if (!workerData.interface) {
		if (!classicWorkersSupported) {
			workerData.worker = getWorker(workerOptions, config.baseURL);
		} else {
			try {
				workerData.worker = getWorker({}, config.baseURL);
			} catch (error) {
				classicWorkersSupported = false;
				workerData.worker = getWorker(workerOptions, config.baseURL);
			}
		}
		workerData.worker.addEventListener(MESSAGE_EVENT_TYPE, onMessage, false);
		workerData.interface = {
			append(data) {
				return initAndSendMessage({ type: MESSAGE_APPEND, data });
			},
			flush() {
				return initAndSendMessage({ type: MESSAGE_FLUSH });
			},
			abort() {
				workerData.onTaskFinished();
			}
		};
	}
	return workerData.interface;

	function getWorker(options, baseURL) {
		let url, scriptUrl;
		url = workerData.scripts[0];
		if (typeof url == "function") {
			url = url();
		}
		try {
			scriptUrl = new URL(url, baseURL);
		} catch (error) {
			scriptUrl = url;
		}
		return new Worker(scriptUrl, options);
	}

	async function initAndSendMessage(message) {
		if (!messageTask) {
			const options = workerData.options;
			const scripts = workerData.scripts.slice(1);
			await sendMessage({ scripts, type: MESSAGE_INIT, options, config: { chunkSize: config.chunkSize } });
		}
		return sendMessage(message);
	}

	function sendMessage(message) {
		const worker = workerData.worker;
		const result = new Promise((resolve, reject) => messageTask = { resolve, reject });
		try {
			if (message.data) {
				try {
					message.data = message.data.buffer;
					worker.postMessage(message, [message.data]);
				} catch (error) {
					worker.postMessage(message);
				}
			} else {
				worker.postMessage(message);
			}
		} catch (error) {
			messageTask.reject(error);
			messageTask = null;
			workerData.onTaskFinished();
		}
		return result;
	}

	function onMessage(event) {
		const message = event.data;
		if (messageTask) {
			const reponseError = message.error;
			const type = message.type;
			if (reponseError) {
				const error = new Error(reponseError.message);
				error.stack = reponseError.stack;
				messageTask.reject(error);
				messageTask = null;
				workerData.onTaskFinished();
			} else if (type == MESSAGE_INIT || type == MESSAGE_FLUSH || type == MESSAGE_APPEND) {
				const data = message.data;
				if (type == MESSAGE_FLUSH) {
					messageTask.resolve({ data: new Uint8Array(data), signature: message.signature });
					messageTask = null;
					workerData.onTaskFinished();
				} else {
					messageTask.resolve(data && new Uint8Array(data));
				}
			}
		}
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

let pool = [];
let pendingRequests = [];

function createCodec(codecConstructor, options, config) {
	const streamCopy = !options.compressed && !options.signed && !options.encrypted;
	const webWorker = !streamCopy && (options.useWebWorkers || (options.useWebWorkers === undefined && config.useWebWorkers));
	const scripts = webWorker && config.workerScripts ? config.workerScripts[options.codecType] : [];
	if (pool.length < config.maxWorkers) {
		const workerData = {};
		pool.push(workerData);
		return getWorker(workerData, codecConstructor, options, config, onTaskFinished, webWorker, scripts);
	} else {
		const workerData = pool.find(workerData => !workerData.busy);
		if (workerData) {
			clearTerminateTimeout(workerData);
			return getWorker(workerData, codecConstructor, options, config, onTaskFinished, webWorker, scripts);
		} else {
			return new Promise(resolve => pendingRequests.push({ resolve, codecConstructor, options, webWorker, scripts }));
		}
	}

	function onTaskFinished(workerData) {
		if (pendingRequests.length) {
			const [{ resolve, codecConstructor, options, webWorker, scripts }] = pendingRequests.splice(0, 1);
			resolve(getWorker(workerData, codecConstructor, options, config, onTaskFinished, webWorker, scripts));
		} else if (workerData.worker) {
			clearTerminateTimeout(workerData);
			if (Number.isFinite(config.terminateWorkerTimeout) && config.terminateWorkerTimeout >= 0) {
				workerData.terminateTimeout = setTimeout(() => {
					pool = pool.filter(data => data != workerData);
					workerData.terminate();
				}, config.terminateWorkerTimeout);
			}
		} else {
			pool = pool.filter(data => data != workerData);
		}
	}
}

function clearTerminateTimeout(workerData) {
	if (workerData.terminateTimeout) {
		clearTimeout(workerData.terminateTimeout);
		workerData.terminateTimeout = null;
	}
}

function terminateWorkers() {
	pool.forEach(workerData => {
		clearTerminateTimeout(workerData);
		workerData.terminate();
	});
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const MINIMUM_CHUNK_SIZE = 64;
const ERR_ABORT = "Abort error";

async function processData(codec, reader, writer, offset, inputLength, config, options) {
	const chunkSize = Math.max(config.chunkSize, MINIMUM_CHUNK_SIZE);
	return processChunk();

	async function processChunk(chunkOffset = 0, outputLength = 0) {
		const signal = options.signal;
		if (chunkOffset < inputLength) {
			testAborted(signal, codec);
			const inputData = await reader.readUint8Array(chunkOffset + offset, Math.min(chunkSize, inputLength - chunkOffset));
			const chunkLength = inputData.length;
			testAborted(signal, codec);
			const data = await codec.append(inputData);
			testAborted(signal, codec);
			outputLength += await writeData(writer, data);
			if (options.onprogress) {
				try {
					options.onprogress(chunkOffset + chunkLength, inputLength);
				} catch (error) {
					// ignored
				}
			}
			return processChunk(chunkOffset + chunkSize, outputLength);
		} else {
			const result = await codec.flush();
			outputLength += await writeData(writer, result.data);
			return { signature: result.signature, length: outputLength };
		}
	}
}

function testAborted(signal, codec) {
	if (signal && signal.aborted) {
		codec.abort();
		throw new Error(ERR_ABORT);
	}
}

async function writeData(writer, data) {
	if (data.length) {
		await writer.writeUint8Array(data);
	}
	return data.length;
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* global Blob, FileReader, atob, btoa, XMLHttpRequest, document, fetch */

const ERR_HTTP_STATUS = "HTTP error ";
const ERR_HTTP_RANGE = "HTTP Range not supported";

const CONTENT_TYPE_TEXT_PLAIN = "text/plain";
const HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
const HTTP_HEADER_CONTENT_RANGE = "Content-Range";
const HTTP_HEADER_ACCEPT_RANGES = "Accept-Ranges";
const HTTP_HEADER_RANGE = "Range";
const HTTP_METHOD_HEAD = "HEAD";
const HTTP_METHOD_GET = "GET";
const HTTP_RANGE_UNIT = "bytes";

class Stream {

	constructor() {
		this.size = 0;
	}

	init() {
		this.initialized = true;
	}
}

class Reader extends Stream {
}

class Writer extends Stream {

	writeUint8Array(array) {
		this.size += array.length;
	}
}

class TextReader extends Reader {

	constructor(text) {
		super();
		this.blobReader = new BlobReader(new Blob([text], { type: CONTENT_TYPE_TEXT_PLAIN }));
	}

	async init() {
		super.init();
		this.blobReader.init();
		this.size = this.blobReader.size;
	}

	async readUint8Array(offset, length) {
		return this.blobReader.readUint8Array(offset, length);
	}
}

class TextWriter extends Writer {

	constructor(encoding) {
		super();
		this.encoding = encoding;
		this.blob = new Blob([], { type: CONTENT_TYPE_TEXT_PLAIN });
	}

	async writeUint8Array(array) {
		super.writeUint8Array(array);
		this.blob = new Blob([this.blob, array.buffer], { type: CONTENT_TYPE_TEXT_PLAIN });
	}

	getData() {
		if (this.blob.text) {
			return this.blob.text();
		} else {
			const reader = new FileReader();
			return new Promise((resolve, reject) => {
				reader.onload = event => resolve(event.target.result);
				reader.onerror = () => reject(reader.error);
				reader.readAsText(this.blob, this.encoding);
			});
		}
	}
}

class Data64URIReader extends Reader {

	constructor(dataURI) {
		super();
		this.dataURI = dataURI;
		let dataEnd = dataURI.length;
		while (dataURI.charAt(dataEnd - 1) == "=") {
			dataEnd--;
		}
		this.dataStart = dataURI.indexOf(",") + 1;
		this.size = Math.floor((dataEnd - this.dataStart) * 0.75);
	}

	async readUint8Array(offset, length) {
		const dataArray = new Uint8Array(length);
		const start = Math.floor(offset / 3) * 4;
		const bytes = atob(this.dataURI.substring(start + this.dataStart, Math.ceil((offset + length) / 3) * 4 + this.dataStart));
		const delta = offset - Math.floor(start / 4) * 3;
		for (let indexByte = delta; indexByte < delta + length; indexByte++) {
			dataArray[indexByte - delta] = bytes.charCodeAt(indexByte);
		}
		return dataArray;
	}
}

class Data64URIWriter extends Writer {

	constructor(contentType) {
		super();
		this.data = "data:" + (contentType || "") + ";base64,";
		this.pending = [];
	}

	async writeUint8Array(array) {
		super.writeUint8Array(array);
		let indexArray = 0;
		let dataString = this.pending;
		const delta = this.pending.length;
		this.pending = "";
		for (indexArray = 0; indexArray < (Math.floor((delta + array.length) / 3) * 3) - delta; indexArray++) {
			dataString += String.fromCharCode(array[indexArray]);
		}
		for (; indexArray < array.length; indexArray++) {
			this.pending += String.fromCharCode(array[indexArray]);
		}
		if (dataString.length > 2) {
			this.data += btoa(dataString);
		} else {
			this.pending = dataString;
		}
	}

	getData() {
		return this.data + btoa(this.pending);
	}
}

class BlobReader extends Reader {

	constructor(blob) {
		super();
		this.blob = blob;
		this.size = blob.size;
	}

	async readUint8Array(offset, length) {
		if (this.blob.arrayBuffer) {
			return new Uint8Array(await this.blob.slice(offset, offset + length).arrayBuffer());
		} else {
			const reader = new FileReader();
			return new Promise((resolve, reject) => {
				reader.onload = event => resolve(new Uint8Array(event.target.result));
				reader.onerror = () => reject(reader.error);
				reader.readAsArrayBuffer(this.blob.slice(offset, offset + length));
			});
		}
	}
}

class BlobWriter extends Writer {

	constructor(contentType) {
		super();
		this.contentType = contentType;
		this.arrayBuffersMaxlength = 8;
		initArrayBuffers(this);
	}

	async writeUint8Array(array) {
		super.writeUint8Array(array);
		if (this.arrayBuffers.length == this.arrayBuffersMaxlength) {
			flushArrayBuffers(this);
		}
		this.arrayBuffers.push(array.buffer);
	}

	getData() {
		if (!this.blob) {
			if (this.arrayBuffers.length) {
				flushArrayBuffers(this);
			}
			this.blob = this.pendingBlob;
			initArrayBuffers(this);
		}
		return this.blob;
	}
}

function initArrayBuffers(blobWriter) {
	blobWriter.pendingBlob = new Blob([], { type: blobWriter.contentType });
	blobWriter.arrayBuffers = [];
}

function flushArrayBuffers(blobWriter) {
	blobWriter.pendingBlob = new Blob([blobWriter.pendingBlob, ...blobWriter.arrayBuffers], { type: blobWriter.contentType });
	blobWriter.arrayBuffers = [];
}

class WritableStreamWriter extends Writer {
	constructor(writableStream) {
		super();
		this.writableStream = writableStream;
		this.writer = writableStream.getWriter();
	}

	async writeUint8Array(array) {
		await this.writer.ready;
		return this.writer.write(array);
	}

	async getData() {
		await this.writer.ready;
		await this.writer.close();
		return this.writableStream;
	}
}

class FetchReader extends Reader {

	constructor(url, options) {
		super();
		this.url = url;
		this.preventHeadRequest = options.preventHeadRequest;
		this.useRangeHeader = options.useRangeHeader;
		this.forceRangeRequests = options.forceRangeRequests;
		this.options = Object.assign({}, options);
		delete this.options.preventHeadRequest;
		delete this.options.useRangeHeader;
		delete this.options.forceRangeRequests;
		delete this.options.useXHR;
	}

	async init() {
		super.init();
		await initHttpReader(this, sendFetchRequest, getFetchRequestData);
	}

	async readUint8Array(index, length) {
		return readUint8ArrayHttpReader(this, index, length, sendFetchRequest, getFetchRequestData);
	}
}

class XHRReader extends Reader {

	constructor(url, options) {
		super();
		this.url = url;
		this.preventHeadRequest = options.preventHeadRequest;
		this.useRangeHeader = options.useRangeHeader;
		this.forceRangeRequests = options.forceRangeRequests;
		this.options = options;
	}

	async init() {
		super.init();
		await initHttpReader(this, sendXMLHttpRequest, getXMLHttpRequestData);
	}

	async readUint8Array(index, length) {
		return readUint8ArrayHttpReader(this, index, length, sendXMLHttpRequest, getXMLHttpRequestData);
	}
}

async function initHttpReader(httpReader, sendRequest, getRequestData) {
	if (isHttpFamily(httpReader.url) && (httpReader.useRangeHeader || httpReader.forceRangeRequests)) {
		const response = await sendRequest(HTTP_METHOD_GET, httpReader, getRangeHeaders(httpReader));
		if (!httpReader.forceRangeRequests && response.headers.get(HTTP_HEADER_ACCEPT_RANGES) != HTTP_RANGE_UNIT) {
			throw new Error(ERR_HTTP_RANGE);
		} else {
			let contentSize;
			const contentRangeHeader = response.headers.get(HTTP_HEADER_CONTENT_RANGE);
			if (contentRangeHeader) {
				const splitHeader = contentRangeHeader.trim().split(/\s*\/\s*/);
				if (splitHeader.length) {
					const headerValue = splitHeader[1];
					if (headerValue && headerValue != "*") {
						contentSize = Number(headerValue);
					}
				}
			}
			if (contentSize === undefined) {
				await getContentLength(httpReader, sendRequest, getRequestData);
			} else {
				httpReader.size = contentSize;
			}
		}
	} else {
		await getContentLength(httpReader, sendRequest, getRequestData);
	}
}

async function readUint8ArrayHttpReader(httpReader, index, length, sendRequest, getRequestData) {
	if (httpReader.useRangeHeader || httpReader.forceRangeRequests) {
		const response = await sendRequest(HTTP_METHOD_GET, httpReader, getRangeHeaders(httpReader, index, length));
		if (response.status != 206) {
			throw new Error(ERR_HTTP_RANGE);
		}
		return new Uint8Array(await response.arrayBuffer());
	} else {
		if (!httpReader.data) {
			await getRequestData(httpReader, httpReader.options);
		}
		return new Uint8Array(httpReader.data.subarray(index, index + length));
	}
}

function getRangeHeaders(httpReader, index = 0, length = 1) {
	return Object.assign({}, getHeaders(httpReader), { [HTTP_HEADER_RANGE]: HTTP_RANGE_UNIT + "=" + index + "-" + (index + length - 1) });
}

function getHeaders(httpReader) {
	let headers = httpReader.options.headers;
	if (headers) {
		if (Symbol.iterator in headers) {
			return Object.fromEntries(headers);
		} else {
			return headers;
		}
	}
}

async function getFetchRequestData(httpReader) {
	await getRequestData(httpReader, sendFetchRequest);
}

async function getXMLHttpRequestData(httpReader) {
	await getRequestData(httpReader, sendXMLHttpRequest);
}

async function getRequestData(httpReader, sendRequest) {
	const response = await sendRequest(HTTP_METHOD_GET, httpReader, getHeaders(httpReader));
	httpReader.data = new Uint8Array(await response.arrayBuffer());
	if (!httpReader.size) {
		httpReader.size = httpReader.data.length;
	}
}

async function getContentLength(httpReader, sendRequest, getRequestData) {
	if (httpReader.preventHeadRequest) {
		await getRequestData(httpReader, httpReader.options);
	} else {
		const response = await sendRequest(HTTP_METHOD_HEAD, httpReader, getHeaders(httpReader));
		const contentLength = response.headers.get(HTTP_HEADER_CONTENT_LENGTH);
		if (contentLength) {
			httpReader.size = Number(contentLength);
		} else {
			await getRequestData(httpReader, httpReader.options);
		}
	}
}

async function sendFetchRequest(method, { options, url }, headers) {
	const response = await fetch(url, Object.assign({}, options, { method, headers }));
	if (response.status < 400) {
		return response;
	} else {
		throw new Error(ERR_HTTP_STATUS + (response.statusText || response.status));
	}
}

function sendXMLHttpRequest(method, { url }, headers) {
	return new Promise((resolve, reject) => {
		const request = new XMLHttpRequest();
		request.addEventListener("load", () => {
			if (request.status < 400) {
				const headers = [];
				request.getAllResponseHeaders().trim().split(/[\r\n]+/).forEach(header => {
					const splitHeader = header.trim().split(/\s*:\s*/);
					splitHeader[0] = splitHeader[0].trim().replace(/^[a-z]|-[a-z]/g, value => value.toUpperCase());
					headers.push(splitHeader);
				});
				resolve({
					status: request.status,
					arrayBuffer: () => request.response,
					headers: new Map(headers)
				});
			} else {
				reject(new Error(ERR_HTTP_STATUS + (request.statusText || request.status)));
			}
		}, false);
		request.addEventListener("error", event => reject(event.detail.error), false);
		request.open(method, url);
		if (headers) {
			for (const entry of Object.entries(headers)) {
				request.setRequestHeader(entry[0], entry[1]);
			}
		}
		request.responseType = "arraybuffer";
		request.send();
	});
}

class HttpReader extends Reader {

	constructor(url, options = {}) {
		super();
		this.url = url;
		if (options.useXHR) {
			this.reader = new XHRReader(url, options);
		} else {
			this.reader = new FetchReader(url, options);
		}
	}

	set size(value) {
		// ignored
	}

	get size() {
		return this.reader.size;
	}

	async init() {
		super.init();
		await this.reader.init();
	}

	async readUint8Array(index, length) {
		return this.reader.readUint8Array(index, length);
	}
}

class HttpRangeReader extends HttpReader {

	constructor(url, options = {}) {
		options.useRangeHeader = true;
		super(url, options);
	}
}


class Uint8ArrayReader extends Reader {

	constructor(array) {
		super();
		this.array = array;
		this.size = array.length;
	}

	async readUint8Array(index, length) {
		return this.array.slice(index, index + length);
	}
}

class Uint8ArrayWriter extends Writer {

	constructor() {
		super();
		this.array = new Uint8Array(0);
	}

	async writeUint8Array(array) {
		super.writeUint8Array(array);
		const previousArray = this.array;
		this.array = new Uint8Array(previousArray.length + array.length);
		this.array.set(previousArray);
		this.array.set(array, previousArray.length);
	}

	getData() {
		return this.array;
	}
}

function isHttpFamily(url) {
	if (typeof document != "undefined") {
		const anchor = document.createElement("a");
		anchor.href = url;
		return anchor.protocol == "http:" || anchor.protocol == "https:";
	} else {
		return /^https?:\/\//i.test(url);
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const MAX_32_BITS = 0xffffffff;
const MAX_16_BITS = 0xffff;
const COMPRESSION_METHOD_DEFLATE = 0x08;
const COMPRESSION_METHOD_STORE = 0x00;
const COMPRESSION_METHOD_AES = 0x63;

const LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50;
const DATA_DESCRIPTOR_RECORD_SIGNATURE = 0x08074b50;
const CENTRAL_FILE_HEADER_SIGNATURE = 0x02014b50;
const END_OF_CENTRAL_DIR_SIGNATURE = 0x06054b50;
const ZIP64_END_OF_CENTRAL_DIR_SIGNATURE = 0x06064b50;
const ZIP64_END_OF_CENTRAL_DIR_LOCATOR_SIGNATURE = 0x07064b50;
const END_OF_CENTRAL_DIR_LENGTH = 22;
const ZIP64_END_OF_CENTRAL_DIR_LOCATOR_LENGTH = 20;
const ZIP64_END_OF_CENTRAL_DIR_LENGTH = 56;
const ZIP64_END_OF_CENTRAL_DIR_TOTAL_LENGTH = END_OF_CENTRAL_DIR_LENGTH + ZIP64_END_OF_CENTRAL_DIR_LOCATOR_LENGTH + ZIP64_END_OF_CENTRAL_DIR_LENGTH;

const ZIP64_TOTAL_NUMBER_OF_DISKS = 1;

const EXTRAFIELD_TYPE_ZIP64 = 0x0001;
const EXTRAFIELD_TYPE_AES = 0x9901;
const EXTRAFIELD_TYPE_NTFS = 0x000a;
const EXTRAFIELD_TYPE_NTFS_TAG1 = 0x0001;
const EXTRAFIELD_TYPE_EXTENDED_TIMESTAMP = 0x5455;
const EXTRAFIELD_TYPE_UNICODE_PATH = 0x7075;
const EXTRAFIELD_TYPE_UNICODE_COMMENT = 0x6375;

const BITFLAG_ENCRYPTED = 0x01;
const BITFLAG_LEVEL = 0x06;
const BITFLAG_DATA_DESCRIPTOR = 0x0008;
const BITFLAG_LANG_ENCODING_FLAG = 0x0800;
const FILE_ATTR_MSDOS_DIR_MASK = 0x10;

const VERSION_DEFLATE = 0x14;
const VERSION_ZIP64 = 0x2D;
const VERSION_AES = 0x33;

const DIRECTORY_SIGNATURE = "/";

const MAX_DATE = new Date(2107, 11, 31);
const MIN_DATE = new Date(1980, 0, 1);

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const CP437 = "\0☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒáíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αßΓπΣσµτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■ ".split("");

var decodeCP437 = stringValue => {
	let result = "";
	for (let indexCharacter = 0; indexCharacter < stringValue.length; indexCharacter++) {
		result += CP437[stringValue[indexCharacter]];
	}
	return result;
};

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

async function decodeText(value, encoding) {
	if (encoding && encoding.trim().toLowerCase() == "cp437") {
		return decodeCP437(value);
	} else if (typeof TextDecoder == "undefined") {
		const fileReader = new FileReader();
		return new Promise((resolve, reject) => {
			fileReader.onload = event => resolve(event.target.result);
			fileReader.onerror = () => reject(fileReader.error);
			fileReader.readAsText(new Blob([value]));
		});
	} else {
		return new TextDecoder(encoding).decode(value);
	}
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const PROPERTY_NAMES = [
	"filename", "rawFilename", "directory", "encrypted", "compressedSize", "uncompressedSize",
	"lastModDate", "rawLastModDate", "comment", "rawComment", "signature", "extraField",
	"rawExtraField", "bitFlag", "extraFieldZip64", "extraFieldUnicodePath", "extraFieldUnicodeComment",
	"extraFieldAES", "filenameUTF8", "commentUTF8", "offset", "zip64", "compressionMethod",
	"extraFieldNTFS", "lastAccessDate", "creationDate", "extraFieldExtendedTimestamp",
	"version", "versionMadeBy", "msDosCompatible", "internalFileAttribute", "externalFileAttribute"];

class Entry {

	constructor(data) {
		PROPERTY_NAMES.forEach(name => this[name] = data[name]);
	}

}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const ERR_BAD_FORMAT = "File format is not recognized";
const ERR_EOCDR_NOT_FOUND = "End of central directory not found";
const ERR_EOCDR_ZIP64_NOT_FOUND = "End of Zip64 central directory not found";
const ERR_EOCDR_LOCATOR_ZIP64_NOT_FOUND = "End of Zip64 central directory locator not found";
const ERR_CENTRAL_DIRECTORY_NOT_FOUND = "Central directory header not found";
const ERR_LOCAL_FILE_HEADER_NOT_FOUND = "Local file header not found";
const ERR_EXTRAFIELD_ZIP64_NOT_FOUND = "Zip64 extra field not found";
const ERR_ENCRYPTED = "File contains encrypted entry";
const ERR_UNSUPPORTED_ENCRYPTION = "Encryption method not supported";
const ERR_UNSUPPORTED_COMPRESSION = "Compression method not supported";
const CHARSET_UTF8 = "utf-8";
const CHARSET_CP437 = "cp437";
const ZIP64_PROPERTIES = ["uncompressedSize", "compressedSize", "offset"];

class ZipReader {

	constructor(reader, options = {}) {
		Object.assign(this, {
			reader,
			options,
			config: getConfiguration()
		});
	}

	async getEntries(options = {}) {
		const zipReader = this;
		const reader = zipReader.reader;
		if (!reader.initialized) {
			await reader.init();
		}
		if (reader.size < END_OF_CENTRAL_DIR_LENGTH) {
			throw new Error(ERR_BAD_FORMAT);
		}
		const endOfDirectoryInfo = await seekSignature(reader, END_OF_CENTRAL_DIR_SIGNATURE, reader.size, END_OF_CENTRAL_DIR_LENGTH, MAX_16_BITS * 16);
		if (!endOfDirectoryInfo) {
			throw new Error(ERR_EOCDR_NOT_FOUND);
		}
		const endOfDirectoryView = getDataView$1(endOfDirectoryInfo);
		let directoryDataLength = getUint32(endOfDirectoryView, 12);
		let directoryDataOffset = getUint32(endOfDirectoryView, 16);
		let filesLength = getUint16(endOfDirectoryView, 8);
		let prependedDataLength = 0;
		if (directoryDataOffset == MAX_32_BITS || directoryDataLength == MAX_32_BITS || filesLength == MAX_16_BITS) {
			const endOfDirectoryLocatorArray = await readUint8Array(reader, endOfDirectoryInfo.offset - ZIP64_END_OF_CENTRAL_DIR_LOCATOR_LENGTH, ZIP64_END_OF_CENTRAL_DIR_LOCATOR_LENGTH);
			const endOfDirectoryLocatorView = getDataView$1(endOfDirectoryLocatorArray);
			if (getUint32(endOfDirectoryLocatorView, 0) != ZIP64_END_OF_CENTRAL_DIR_LOCATOR_SIGNATURE) {
				throw new Error(ERR_EOCDR_ZIP64_NOT_FOUND);
			}
			directoryDataOffset = getBigUint64(endOfDirectoryLocatorView, 8);
			let endOfDirectoryArray = await readUint8Array(reader, directoryDataOffset, ZIP64_END_OF_CENTRAL_DIR_LENGTH);
			let endOfDirectoryView = getDataView$1(endOfDirectoryArray);
			const expectedDirectoryDataOffset = endOfDirectoryInfo.offset - ZIP64_END_OF_CENTRAL_DIR_LOCATOR_LENGTH - ZIP64_END_OF_CENTRAL_DIR_LENGTH;
			if (getUint32(endOfDirectoryView, 0) != ZIP64_END_OF_CENTRAL_DIR_SIGNATURE && directoryDataOffset != expectedDirectoryDataOffset) {
				const originalDirectoryDataOffset = directoryDataOffset;
				directoryDataOffset = expectedDirectoryDataOffset;
				prependedDataLength = directoryDataOffset - originalDirectoryDataOffset;
				endOfDirectoryArray = await readUint8Array(reader, directoryDataOffset, ZIP64_END_OF_CENTRAL_DIR_LENGTH);
				endOfDirectoryView = getDataView$1(endOfDirectoryArray);
			}
			if (getUint32(endOfDirectoryView, 0) != ZIP64_END_OF_CENTRAL_DIR_SIGNATURE) {
				throw new Error(ERR_EOCDR_LOCATOR_ZIP64_NOT_FOUND);
			}
			filesLength = getBigUint64(endOfDirectoryView, 32);
			directoryDataLength = getBigUint64(endOfDirectoryView, 40);
			directoryDataOffset -= directoryDataLength;
		}
		if (directoryDataOffset < 0 || directoryDataOffset >= reader.size) {
			throw new Error(ERR_BAD_FORMAT);
		}
		let offset = 0;
		let directoryArray = await readUint8Array(reader, directoryDataOffset, directoryDataLength);
		let directoryView = getDataView$1(directoryArray);
		if (directoryDataLength) {
			const expectedDirectoryDataOffset = endOfDirectoryInfo.offset - directoryDataLength;
			if (getUint32(directoryView, offset) != CENTRAL_FILE_HEADER_SIGNATURE && directoryDataOffset != expectedDirectoryDataOffset) {
				const originalDirectoryDataOffset = directoryDataOffset;
				directoryDataOffset = expectedDirectoryDataOffset;
				prependedDataLength = directoryDataOffset - originalDirectoryDataOffset;
				directoryArray = await readUint8Array(reader, directoryDataOffset, directoryDataLength);
				directoryView = getDataView$1(directoryArray);
			}
		}
		if (directoryDataOffset < 0 || directoryDataOffset >= reader.size) {
			throw new Error(ERR_BAD_FORMAT);
		}
		const entries = [];
		for (let indexFile = 0; indexFile < filesLength; indexFile++) {
			const fileEntry = new ZipEntry(reader, zipReader.config, zipReader.options);
			if (getUint32(directoryView, offset) != CENTRAL_FILE_HEADER_SIGNATURE) {
				throw new Error(ERR_CENTRAL_DIRECTORY_NOT_FOUND);
			}
			readCommonHeader(fileEntry, directoryView, offset + 6);
			const languageEncodingFlag = Boolean(fileEntry.bitFlag.languageEncodingFlag);
			const filenameOffset = offset + 46;
			const extraFieldOffset = filenameOffset + fileEntry.filenameLength;
			const commentOffset = extraFieldOffset + fileEntry.extraFieldLength;
			const versionMadeBy = getUint16(directoryView, offset + 4);
			const msDosCompatible = (versionMadeBy & 0) == 0;
			Object.assign(fileEntry, {
				versionMadeBy,
				msDosCompatible,
				compressedSize: 0,
				uncompressedSize: 0,
				commentLength: getUint16(directoryView, offset + 32),
				directory: msDosCompatible && ((getUint8(directoryView, offset + 38) & FILE_ATTR_MSDOS_DIR_MASK) == FILE_ATTR_MSDOS_DIR_MASK),
				offset: getUint32(directoryView, offset + 42) + prependedDataLength,
				internalFileAttribute: getUint32(directoryView, offset + 34),
				externalFileAttribute: getUint32(directoryView, offset + 38),
				rawFilename: directoryArray.subarray(filenameOffset, extraFieldOffset),
				filenameUTF8: languageEncodingFlag,
				commentUTF8: languageEncodingFlag,
				rawExtraField: directoryArray.subarray(extraFieldOffset, commentOffset)
			});
			const endOffset = commentOffset + fileEntry.commentLength;
			fileEntry.rawComment = directoryArray.subarray(commentOffset, endOffset);
			const filenameEncoding = getOptionValue$1(zipReader, options, "filenameEncoding");
			const commentEncoding = getOptionValue$1(zipReader, options, "commentEncoding");
			const [filename, comment] = await Promise.all([
				decodeText(fileEntry.rawFilename, fileEntry.filenameUTF8 ? CHARSET_UTF8 : filenameEncoding || CHARSET_CP437),
				decodeText(fileEntry.rawComment, fileEntry.commentUTF8 ? CHARSET_UTF8 : commentEncoding || CHARSET_CP437)
			]);
			fileEntry.filename = filename;
			fileEntry.comment = comment;
			if (!fileEntry.directory && fileEntry.filename.endsWith(DIRECTORY_SIGNATURE)) {
				fileEntry.directory = true;
			}
			await readCommonFooter(fileEntry, fileEntry, directoryView, offset + 6);
			const entry = new Entry(fileEntry);
			entry.getData = (writer, options) => fileEntry.getData(writer, entry, options);
			entries.push(entry);
			offset = endOffset;
			if (options.onprogress) {
				try {
					options.onprogress(indexFile + 1, filesLength, new Entry(fileEntry));
				} catch (error) {
					// ignored
				}
			}
		}
		return entries;
	}

	async close() {
	}
}

class ZipEntry {

	constructor(reader, config, options) {
		Object.assign(this, {
			reader,
			config,
			options
		});
	}

	async getData(writer, fileEntry, options = {}) {
		const zipEntry = this;
		const {
			reader,
			offset,
			extraFieldAES,
			compressionMethod,
			config,
			bitFlag,
			signature,
			rawLastModDate,
			compressedSize
		} = zipEntry;
		const localDirectory = zipEntry.localDirectory = {};
		if (!reader.initialized) {
			await reader.init();
		}
		let dataArray = await readUint8Array(reader, offset, 30);
		const dataView = getDataView$1(dataArray);
		let password = getOptionValue$1(zipEntry, options, "password");
		password = password && password.length && password;
		if (extraFieldAES) {
			if (extraFieldAES.originalCompressionMethod != COMPRESSION_METHOD_AES) {
				throw new Error(ERR_UNSUPPORTED_COMPRESSION);
			}
		}
		if (compressionMethod != COMPRESSION_METHOD_STORE && compressionMethod != COMPRESSION_METHOD_DEFLATE) {
			throw new Error(ERR_UNSUPPORTED_COMPRESSION);
		}
		if (getUint32(dataView, 0) != LOCAL_FILE_HEADER_SIGNATURE) {
			throw new Error(ERR_LOCAL_FILE_HEADER_NOT_FOUND);
		}
		readCommonHeader(localDirectory, dataView, 4);
		dataArray = await readUint8Array(reader, offset, 30 + localDirectory.filenameLength + localDirectory.extraFieldLength);
		localDirectory.rawExtraField = dataArray.subarray(30 + localDirectory.filenameLength);
		await readCommonFooter(zipEntry, localDirectory, dataView, 4);
		fileEntry.lastAccessDate = localDirectory.lastAccessDate;
		fileEntry.creationDate = localDirectory.creationDate;
		const encrypted = zipEntry.encrypted && localDirectory.encrypted;
		const zipCrypto = encrypted && !extraFieldAES;
		if (encrypted) {
			if (!zipCrypto && extraFieldAES.strength === undefined) {
				throw new Error(ERR_UNSUPPORTED_ENCRYPTION);
			} else if (!password) {
				throw new Error(ERR_ENCRYPTED);
			}
		}
		const codec = await createCodec(config.Inflate, {
			codecType: CODEC_INFLATE,
			password,
			zipCrypto,
			encryptionStrength: extraFieldAES && extraFieldAES.strength,
			signed: getOptionValue$1(zipEntry, options, "checkSignature"),
			passwordVerification: zipCrypto && (bitFlag.dataDescriptor ? ((rawLastModDate >>> 8) & 0xFF) : ((signature >>> 24) & 0xFF)),
			signature,
			compressed: compressionMethod != 0,
			encrypted,
			useWebWorkers: getOptionValue$1(zipEntry, options, "useWebWorkers")
		}, config);
		if (!writer.initialized) {
			await writer.init();
		}
		const signal = getOptionValue$1(zipEntry, options, "signal");
		const dataOffset = offset + 30 + localDirectory.filenameLength + localDirectory.extraFieldLength;
		await processData(codec, reader, writer, dataOffset, compressedSize, config, { onprogress: options.onprogress, signal });
		return writer.getData();
	}
}

function readCommonHeader(directory, dataView, offset) {
	const rawBitFlag = directory.rawBitFlag = getUint16(dataView, offset + 2);
	const encrypted = (rawBitFlag & BITFLAG_ENCRYPTED) == BITFLAG_ENCRYPTED;
	const rawLastModDate = getUint32(dataView, offset + 6);
	Object.assign(directory, {
		encrypted,
		version: getUint16(dataView, offset),
		bitFlag: {
			level: (rawBitFlag & BITFLAG_LEVEL) >> 1,
			dataDescriptor: (rawBitFlag & BITFLAG_DATA_DESCRIPTOR) == BITFLAG_DATA_DESCRIPTOR,
			languageEncodingFlag: (rawBitFlag & BITFLAG_LANG_ENCODING_FLAG) == BITFLAG_LANG_ENCODING_FLAG
		},
		rawLastModDate,
		lastModDate: getDate(rawLastModDate),
		filenameLength: getUint16(dataView, offset + 22),
		extraFieldLength: getUint16(dataView, offset + 24)
	});
}

async function readCommonFooter(fileEntry, directory, dataView, offset) {
	const rawExtraField = directory.rawExtraField;
	const extraField = directory.extraField = new Map();
	const rawExtraFieldView = getDataView$1(new Uint8Array(rawExtraField));
	let offsetExtraField = 0;
	try {
		while (offsetExtraField < rawExtraField.length) {
			const type = getUint16(rawExtraFieldView, offsetExtraField);
			const size = getUint16(rawExtraFieldView, offsetExtraField + 2);
			extraField.set(type, {
				type,
				data: rawExtraField.slice(offsetExtraField + 4, offsetExtraField + 4 + size)
			});
			offsetExtraField += 4 + size;
		}
	} catch (error) {
		// ignored
	}
	const compressionMethod = getUint16(dataView, offset + 4);
	directory.signature = getUint32(dataView, offset + 10);
	directory.uncompressedSize = getUint32(dataView, offset + 18);
	directory.compressedSize = getUint32(dataView, offset + 14);
	const extraFieldZip64 = extraField.get(EXTRAFIELD_TYPE_ZIP64);
	if (extraFieldZip64) {
		readExtraFieldZip64(extraFieldZip64, directory);
		directory.extraFieldZip64 = extraFieldZip64;
	}
	const extraFieldUnicodePath = extraField.get(EXTRAFIELD_TYPE_UNICODE_PATH);
	if (extraFieldUnicodePath) {
		await readExtraFieldUnicode(extraFieldUnicodePath, "filename", "rawFilename", directory, fileEntry);
		directory.extraFieldUnicodePath = extraFieldUnicodePath;
	}
	const extraFieldUnicodeComment = extraField.get(EXTRAFIELD_TYPE_UNICODE_COMMENT);
	if (extraFieldUnicodeComment) {
		await readExtraFieldUnicode(extraFieldUnicodeComment, "comment", "rawComment", directory, fileEntry);
		directory.extraFieldUnicodeComment = extraFieldUnicodeComment;
	}
	const extraFieldAES = extraField.get(EXTRAFIELD_TYPE_AES);
	if (extraFieldAES) {
		readExtraFieldAES(extraFieldAES, directory, compressionMethod);
		directory.extraFieldAES = extraFieldAES;
	} else {
		directory.compressionMethod = compressionMethod;
	}
	const extraFieldNTFS = extraField.get(EXTRAFIELD_TYPE_NTFS);
	if (extraFieldNTFS) {
		readExtraFieldNTFS(extraFieldNTFS, directory);
		directory.extraFieldNTFS = extraFieldNTFS;
	}
	const extraFieldExtendedTimestamp = extraField.get(EXTRAFIELD_TYPE_EXTENDED_TIMESTAMP);
	if (extraFieldExtendedTimestamp) {
		readExtraFieldExtendedTimestamp(extraFieldExtendedTimestamp, directory);
		directory.extraFieldExtendedTimestamp = extraFieldExtendedTimestamp;
	}
}

function readExtraFieldZip64(extraFieldZip64, directory) {
	directory.zip64 = true;
	const extraFieldView = getDataView$1(extraFieldZip64.data);
	extraFieldZip64.values = [];
	for (let indexValue = 0; indexValue < Math.floor(extraFieldZip64.data.length / 8); indexValue++) {
		extraFieldZip64.values.push(getBigUint64(extraFieldView, 0 + indexValue * 8));
	}
	const missingProperties = ZIP64_PROPERTIES.filter(propertyName => directory[propertyName] == MAX_32_BITS);
	for (let indexMissingProperty = 0; indexMissingProperty < missingProperties.length; indexMissingProperty++) {
		extraFieldZip64[missingProperties[indexMissingProperty]] = extraFieldZip64.values[indexMissingProperty];
	}
	ZIP64_PROPERTIES.forEach(propertyName => {
		if (directory[propertyName] == MAX_32_BITS) {
			if (extraFieldZip64[propertyName] !== undefined) {
				directory[propertyName] = extraFieldZip64[propertyName];
			} else {
				throw new Error(ERR_EXTRAFIELD_ZIP64_NOT_FOUND);
			}
		}
	});
}

async function readExtraFieldUnicode(extraFieldUnicode, propertyName, rawPropertyName, directory, fileEntry) {
	const extraFieldView = getDataView$1(extraFieldUnicode.data);
	extraFieldUnicode.version = getUint8(extraFieldView, 0);
	extraFieldUnicode.signature = getUint32(extraFieldView, 1);
	const crc32 = new Crc32();
	crc32.append(fileEntry[rawPropertyName]);
	const dataViewSignature = getDataView$1(new Uint8Array(4));
	dataViewSignature.setUint32(0, crc32.get(), true);
	extraFieldUnicode[propertyName] = await decodeText(extraFieldUnicode.data.subarray(5));
	extraFieldUnicode.valid = !fileEntry.bitFlag.languageEncodingFlag && extraFieldUnicode.signature == getUint32(dataViewSignature, 0);
	if (extraFieldUnicode.valid) {
		directory[propertyName] = extraFieldUnicode[propertyName];
		directory[propertyName + "UTF8"] = true;
	}
}

function readExtraFieldAES(extraFieldAES, directory, compressionMethod) {
	const extraFieldView = getDataView$1(extraFieldAES.data);
	extraFieldAES.vendorVersion = getUint8(extraFieldView, 0);
	extraFieldAES.vendorId = getUint8(extraFieldView, 2);
	const strength = getUint8(extraFieldView, 4);
	extraFieldAES.strength = strength;
	extraFieldAES.originalCompressionMethod = compressionMethod;
	directory.compressionMethod = extraFieldAES.compressionMethod = getUint16(extraFieldView, 5);
}

function readExtraFieldNTFS(extraFieldNTFS, directory) {
	const extraFieldView = getDataView$1(extraFieldNTFS.data);
	let offsetExtraField = 4;
	let tag1Data;
	try {
		while (offsetExtraField < extraFieldNTFS.data.length && !tag1Data) {
			const tagValue = getUint16(extraFieldView, offsetExtraField);
			const attributeSize = getUint16(extraFieldView, offsetExtraField + 2);
			if (tagValue == EXTRAFIELD_TYPE_NTFS_TAG1) {
				tag1Data = extraFieldNTFS.data.slice(offsetExtraField + 4, offsetExtraField + 4 + attributeSize);
			}
			offsetExtraField += 4 + attributeSize;
		}
	} catch (error) {
		// ignored
	}
	try {
		if (tag1Data && tag1Data.length == 24) {
			const tag1View = getDataView$1(tag1Data);
			const rawLastModDate = tag1View.getBigUint64(0, true);
			const rawLastAccessDate = tag1View.getBigUint64(8, true);
			const rawCreationDate = tag1View.getBigUint64(16, true);
			Object.assign(extraFieldNTFS, {
				rawLastModDate,
				rawLastAccessDate,
				rawCreationDate
			});
			const lastModDate = getDateNTFS(rawLastModDate);
			const lastAccessDate = getDateNTFS(rawLastAccessDate);
			const creationDate = getDateNTFS(rawCreationDate);
			const extraFieldData = { lastModDate, lastAccessDate, creationDate };
			Object.assign(extraFieldNTFS, extraFieldData);
			Object.assign(directory, extraFieldData);
		}
	} catch (error) {
		// ignored
	}
}

function readExtraFieldExtendedTimestamp(extraFieldExtendedTimestamp, directory) {
	const extraFieldView = getDataView$1(extraFieldExtendedTimestamp.data);
	const flags = getUint8(extraFieldView, 0);
	const timeProperties = [];
	const timeRawProperties = [];
	if ((flags & 0x1) == 0x1) {
		timeProperties.push("lastModDate");
		timeRawProperties.push("rawLastModDate");
	}
	if ((flags & 0x2) == 0x2) {
		timeProperties.push("lastAccessDate");
		timeRawProperties.push("rawLastAccessDate");
	}
	if ((flags & 0x4) == 0x4) {
		timeProperties.push("creationDate");
		timeRawProperties.push("rawCreationDate");
	}
	let offset = 1;
	timeProperties.forEach((propertyName, indexProperty) => {
		if (extraFieldExtendedTimestamp.data.length >= offset + 4) {
			const time = getUint32(extraFieldView, offset);
			directory[propertyName] = extraFieldExtendedTimestamp[propertyName] = new Date(time * 1000);
			const rawPropertyName = timeRawProperties[indexProperty];
			extraFieldExtendedTimestamp[rawPropertyName] = time;
		}
		offset += 4;
	});
}

async function seekSignature(reader, signature, startOffset, minimumBytes, maximumLength) {
	const signatureArray = new Uint8Array(4);
	const signatureView = getDataView$1(signatureArray);
	setUint32$1(signatureView, 0, signature);
	const maximumBytes = minimumBytes + maximumLength;
	return (await seek(minimumBytes)) || await seek(Math.min(maximumBytes, startOffset));

	async function seek(length) {
		const offset = startOffset - length;
		const bytes = await readUint8Array(reader, offset, length);
		for (let indexByte = bytes.length - minimumBytes; indexByte >= 0; indexByte--) {
			if (bytes[indexByte] == signatureArray[0] && bytes[indexByte + 1] == signatureArray[1] &&
				bytes[indexByte + 2] == signatureArray[2] && bytes[indexByte + 3] == signatureArray[3]) {
				return {
					offset: offset + indexByte,
					buffer: bytes.slice(indexByte, indexByte + minimumBytes).buffer
				};
			}
		}
	}
}

function getOptionValue$1(zipReader, options, name) {
	return options[name] === undefined ? zipReader.options[name] : options[name];
}

function getDate(timeRaw) {
	const date = (timeRaw & 0xffff0000) >> 16, time = timeRaw & 0x0000ffff;
	try {
		return new Date(1980 + ((date & 0xFE00) >> 9), ((date & 0x01E0) >> 5) - 1, date & 0x001F, (time & 0xF800) >> 11, (time & 0x07E0) >> 5, (time & 0x001F) * 2, 0);
	} catch (error) {
		// ignored
	}
}

function getDateNTFS(timeRaw) {
	return new Date((Number((timeRaw / BigInt(10000)) - BigInt(11644473600000))));
}

function getUint8(view, offset) {
	return view.getUint8(offset);
}

function getUint16(view, offset) {
	return view.getUint16(offset, true);
}

function getUint32(view, offset) {
	return view.getUint32(offset, true);
}

function getBigUint64(view, offset) {
	return Number(view.getBigUint64(offset, true));
}

function setUint32$1(view, offset, value) {
	view.setUint32(offset, value, true);
}

function getDataView$1(array) {
	return new DataView(array.buffer);
}

function readUint8Array(reader, offset, size) {
	return reader.readUint8Array(offset, size);
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const ERR_DUPLICATED_NAME = "File already exists";
const ERR_INVALID_COMMENT = "Zip file comment exceeds 64KB";
const ERR_INVALID_ENTRY_COMMENT = "File entry comment exceeds 64KB";
const ERR_INVALID_ENTRY_NAME = "File entry name exceeds 64KB";
const ERR_INVALID_VERSION = "Version exceeds 65535";
const ERR_INVALID_ENCRYPTION_STRENGTH = "The strength must equal 1, 2, or 3";
const ERR_INVALID_EXTRAFIELD_TYPE = "Extra field type exceeds 65535";
const ERR_INVALID_EXTRAFIELD_DATA = "Extra field data exceeds 64KB";
const ERR_UNSUPPORTED_FORMAT = "Zip64 is not supported";

const EXTRAFIELD_DATA_AES = new Uint8Array([0x07, 0x00, 0x02, 0x00, 0x41, 0x45, 0x03, 0x00, 0x00]);
const EXTRAFIELD_LENGTH_ZIP64 = 24;

let workers = 0;

class ZipWriter {

	constructor(writer, options = {}) {
		Object.assign(this, {
			writer,
			options,
			config: getConfiguration(),
			files: new Map(),
			offset: writer.size,
			pendingCompressedSize: 0,
			pendingEntries: []
		});
	}

	async add(name = "", reader, options = {}) {
		const zipWriter = this;
		if (workers < zipWriter.config.maxWorkers) {
			workers++;
			try {
				return await addFile(zipWriter, name, reader, options);
			} finally {
				workers--;
				const pendingEntry = zipWriter.pendingEntries.shift();
				if (pendingEntry) {
					zipWriter.add(pendingEntry.name, pendingEntry.reader, pendingEntry.options)
						.then(pendingEntry.resolve)
						.catch(pendingEntry.reject);
				}
			}
		} else {
			return new Promise((resolve, reject) => zipWriter.pendingEntries.push({ name, reader, options, resolve, reject }));
		}
	}

	async close(comment = new Uint8Array(0), options = {}) {
		await closeFile(this, comment, options);
		return this.writer.getData();
	}
}

async function addFile(zipWriter, name, reader, options) {
	name = name.trim();
	if (options.directory && (!name.endsWith(DIRECTORY_SIGNATURE))) {
		name += DIRECTORY_SIGNATURE;
	} else {
		options.directory = name.endsWith(DIRECTORY_SIGNATURE);
	}
	if (zipWriter.files.has(name)) {
		throw new Error(ERR_DUPLICATED_NAME);
	}
	const rawFilename = encodeText(name);
	if (rawFilename.length > MAX_16_BITS) {
		throw new Error(ERR_INVALID_ENTRY_NAME);
	}
	const comment = options.comment || "";
	const rawComment = encodeText(comment);
	if (rawComment.length > MAX_16_BITS) {
		throw new Error(ERR_INVALID_ENTRY_COMMENT);
	}
	const version = zipWriter.options.version || options.version || 0;
	if (version > MAX_16_BITS) {
		throw new Error(ERR_INVALID_VERSION);
	}
	const versionMadeBy = zipWriter.options.versionMadeBy || options.versionMadeBy || 20;
	if (versionMadeBy > MAX_16_BITS) {
		throw new Error(ERR_INVALID_VERSION);
	}
	const lastModDate = getOptionValue(zipWriter, options, "lastModDate") || new Date();
	const lastAccessDate = getOptionValue(zipWriter, options, "lastAccessDate");
	const creationDate = getOptionValue(zipWriter, options, "creationDate");
	const password = getOptionValue(zipWriter, options, "password");
	const encryptionStrength = getOptionValue(zipWriter, options, "encryptionStrength") || 3;
	const zipCrypto = getOptionValue(zipWriter, options, "zipCrypto");
	if (password !== undefined && encryptionStrength !== undefined && (encryptionStrength < 1 || encryptionStrength > 3)) {
		throw new Error(ERR_INVALID_ENCRYPTION_STRENGTH);
	}
	let rawExtraField = new Uint8Array(0);
	const extraField = options.extraField;
	if (extraField) {
		let extraFieldSize = 0;
		let offset = 0;
		extraField.forEach(data => extraFieldSize += 4 + data.length);
		rawExtraField = new Uint8Array(extraFieldSize);
		extraField.forEach((data, type) => {
			if (type > MAX_16_BITS) {
				throw new Error(ERR_INVALID_EXTRAFIELD_TYPE);
			}
			if (data.length > MAX_16_BITS) {
				throw new Error(ERR_INVALID_EXTRAFIELD_DATA);
			}
			arraySet(rawExtraField, new Uint16Array([type]), offset);
			arraySet(rawExtraField, new Uint16Array([data.length]), offset + 2);
			arraySet(rawExtraField, data, offset + 4);
			offset += 4 + data.length;
		});
	}
	let extendedTimestamp = getOptionValue(zipWriter, options, "extendedTimestamp");
	if (extendedTimestamp === undefined) {
		extendedTimestamp = true;
	}
	let maximumCompressedSize = 0;
	let keepOrder = getOptionValue(zipWriter, options, "keepOrder");
	if (keepOrder === undefined) {
		keepOrder = true;
	}
	let uncompressedSize = 0;
	let msDosCompatible = getOptionValue(zipWriter, options, "msDosCompatible");
	if (msDosCompatible === undefined) {
		msDosCompatible = true;
	}
	const internalFileAttribute = getOptionValue(zipWriter, options, "internalFileAttribute") || 0;
	const externalFileAttribute = getOptionValue(zipWriter, options, "externalFileAttribute") || 0;
	if (reader) {
		if (!reader.initialized) {
			await reader.init();
		}
		uncompressedSize = reader.size;
		maximumCompressedSize = getMaximumCompressedSize(uncompressedSize);
	}
	let zip64 = options.zip64 || zipWriter.options.zip64 || false;
	if (zipWriter.offset + zipWriter.pendingCompressedSize >= MAX_32_BITS ||
		uncompressedSize >= MAX_32_BITS ||
		maximumCompressedSize >= MAX_32_BITS) {
		if (options.zip64 === false || zipWriter.options.zip64 === false || !keepOrder) {
			throw new Error(ERR_UNSUPPORTED_FORMAT);
		} else {
			zip64 = true;
		}
	}
	zipWriter.pendingCompressedSize += maximumCompressedSize;
	await Promise.resolve();
	const level = getOptionValue(zipWriter, options, "level");
	const useWebWorkers = getOptionValue(zipWriter, options, "useWebWorkers");
	const bufferedWrite = getOptionValue(zipWriter, options, "bufferedWrite");
	let dataDescriptor = getOptionValue(zipWriter, options, "dataDescriptor");
	let dataDescriptorSignature = getOptionValue(zipWriter, options, "dataDescriptorSignature");
	const signal = getOptionValue(zipWriter, options, "signal");
	if (dataDescriptor === undefined) {
		dataDescriptor = true;
	}
	if (dataDescriptor && dataDescriptorSignature === undefined) {
		dataDescriptorSignature = true;
	}
	const fileEntry = await getFileEntry(zipWriter, name, reader, Object.assign({}, options, {
		rawFilename,
		rawComment,
		version,
		versionMadeBy,
		lastModDate,
		lastAccessDate,
		creationDate,
		rawExtraField,
		zip64,
		password,
		level,
		useWebWorkers,
		encryptionStrength,
		extendedTimestamp,
		zipCrypto,
		bufferedWrite,
		keepOrder,
		dataDescriptor,
		dataDescriptorSignature,
		signal,
		msDosCompatible,
		internalFileAttribute,
		externalFileAttribute
	}));
	if (maximumCompressedSize) {
		zipWriter.pendingCompressedSize -= maximumCompressedSize;
	}
	Object.assign(fileEntry, { name, comment, extraField });
	return new Entry(fileEntry);
}

async function getFileEntry(zipWriter, name, reader, options) {
	const files = zipWriter.files;
	const writer = zipWriter.writer;
	const previousFileEntry = Array.from(files.values()).pop();
	let fileEntry = {};
	let bufferedWrite;
	let resolveLockUnbufferedWrite;
	let resolveLockCurrentFileEntry;
	files.set(name, fileEntry);
	try {
		let lockPreviousFileEntry;
		let fileWriter;
		let lockCurrentFileEntry;
		if (options.keepOrder) {
			lockPreviousFileEntry = previousFileEntry && previousFileEntry.lock;
		}
		fileEntry.lock = lockCurrentFileEntry = new Promise(resolve => resolveLockCurrentFileEntry = resolve);
		if (options.bufferedWrite || zipWriter.lockWrite || !options.dataDescriptor) {
			fileWriter = new BlobWriter();
			fileWriter.init();
			bufferedWrite = true;
		} else {
			zipWriter.lockWrite = new Promise(resolve => resolveLockUnbufferedWrite = resolve);
			if (!writer.initialized) {
				await writer.init();
			}
			fileWriter = writer;
		}
		fileEntry = await createFileEntry(reader, fileWriter, zipWriter.config, options);
		fileEntry.lock = lockCurrentFileEntry;
		files.set(name, fileEntry);
		fileEntry.filename = name;
		if (bufferedWrite) {
			let indexWrittenData = 0;
			const blob = fileWriter.getData();
			await Promise.all([zipWriter.lockWrite, lockPreviousFileEntry]);
			let pendingFileEntry;
			do {
				pendingFileEntry = Array.from(files.values()).find(fileEntry => fileEntry.writingBufferedData);
				if (pendingFileEntry) {
					await pendingFileEntry.lock;
				}
			} while (pendingFileEntry && pendingFileEntry.lock);
			fileEntry.writingBufferedData = true;
			if (!options.dataDescriptor) {
				const headerLength = 26;
				const arrayBuffer = await sliceAsArrayBuffer(blob, 0, headerLength);
				const arrayBufferView = new DataView(arrayBuffer);
				if (!fileEntry.encrypted || options.zipCrypto) {
					setUint32(arrayBufferView, 14, fileEntry.signature);
				}
				if (fileEntry.zip64) {
					setUint32(arrayBufferView, 18, MAX_32_BITS);
					setUint32(arrayBufferView, 22, MAX_32_BITS);
				} else {
					setUint32(arrayBufferView, 18, fileEntry.compressedSize);
					setUint32(arrayBufferView, 22, fileEntry.uncompressedSize);
				}
				await writer.writeUint8Array(new Uint8Array(arrayBuffer));
				indexWrittenData = headerLength;
			}
			await writeBlob(writer, blob, indexWrittenData);
			delete fileEntry.writingBufferedData;
		}
		fileEntry.offset = zipWriter.offset;
		if (fileEntry.zip64) {
			const rawExtraFieldZip64View = getDataView(fileEntry.rawExtraFieldZip64);
			setBigUint64(rawExtraFieldZip64View, 20, BigInt(fileEntry.offset));
		} else if (fileEntry.offset >= MAX_32_BITS) {
			throw new Error(ERR_UNSUPPORTED_FORMAT);
		}
		zipWriter.offset += fileEntry.length;
		return fileEntry;
	} catch (error) {
		if ((bufferedWrite && fileEntry.writingBufferedData) || (!bufferedWrite && fileEntry.dataWritten)) {
			error.corruptedEntry = zipWriter.hasCorruptedEntries = true;
			if (fileEntry.uncompressedSize) {
				zipWriter.offset += fileEntry.uncompressedSize;
			}
		}
		files.delete(name);
		throw error;
	} finally {
		resolveLockCurrentFileEntry();
		if (resolveLockUnbufferedWrite) {
			resolveLockUnbufferedWrite();
		}
	}
}

async function createFileEntry(reader, writer, config, options) {
	const {
		rawFilename,
		lastAccessDate,
		creationDate,
		password,
		level,
		zip64,
		zipCrypto,
		dataDescriptor,
		dataDescriptorSignature,
		directory,
		version,
		versionMadeBy,
		rawComment,
		rawExtraField,
		useWebWorkers,
		onprogress,
		signal,
		encryptionStrength,
		extendedTimestamp,
		msDosCompatible,
		internalFileAttribute,
		externalFileAttribute
	} = options;
	const encrypted = Boolean(password && password.length);
	const compressed = level !== 0 && !directory;
	let rawExtraFieldAES;
	if (encrypted && !zipCrypto) {
		rawExtraFieldAES = new Uint8Array(EXTRAFIELD_DATA_AES.length + 2);
		const extraFieldAESView = getDataView(rawExtraFieldAES);
		setUint16(extraFieldAESView, 0, EXTRAFIELD_TYPE_AES);
		arraySet(rawExtraFieldAES, EXTRAFIELD_DATA_AES, 2);
		setUint8(extraFieldAESView, 8, encryptionStrength);
	} else {
		rawExtraFieldAES = new Uint8Array(0);
	}
	let rawExtraFieldNTFS;
	let rawExtraFieldExtendedTimestamp;
	if (extendedTimestamp) {
		rawExtraFieldExtendedTimestamp = new Uint8Array(9 + (lastAccessDate ? 4 : 0) + (creationDate ? 4 : 0));
		const extraFieldExtendedTimestampView = getDataView(rawExtraFieldExtendedTimestamp);
		setUint16(extraFieldExtendedTimestampView, 0, EXTRAFIELD_TYPE_EXTENDED_TIMESTAMP);
		setUint16(extraFieldExtendedTimestampView, 2, rawExtraFieldExtendedTimestamp.length - 4);
		const extraFieldExtendedTimestampFlag = 0x1 + (lastAccessDate ? 0x2 : 0) + (creationDate ? 0x4 : 0);
		setUint8(extraFieldExtendedTimestampView, 4, extraFieldExtendedTimestampFlag);
		setUint32(extraFieldExtendedTimestampView, 5, Math.floor(options.lastModDate.getTime() / 1000));
		if (lastAccessDate) {
			setUint32(extraFieldExtendedTimestampView, 9, Math.floor(lastAccessDate.getTime() / 1000));
		}
		if (creationDate) {
			setUint32(extraFieldExtendedTimestampView, 13, Math.floor(creationDate.getTime() / 1000));
		}
		try {
			rawExtraFieldNTFS = new Uint8Array(36);
			const extraFieldNTFSView = getDataView(rawExtraFieldNTFS);
			const lastModTimeNTFS = getTimeNTFS(options.lastModDate);
			setUint16(extraFieldNTFSView, 0, EXTRAFIELD_TYPE_NTFS);
			setUint16(extraFieldNTFSView, 2, 32);
			setUint16(extraFieldNTFSView, 8, EXTRAFIELD_TYPE_NTFS_TAG1);
			setUint16(extraFieldNTFSView, 10, 24);
			setBigUint64(extraFieldNTFSView, 12, lastModTimeNTFS);
			setBigUint64(extraFieldNTFSView, 20, getTimeNTFS(lastAccessDate) || lastModTimeNTFS);
			setBigUint64(extraFieldNTFSView, 28, getTimeNTFS(creationDate) || lastModTimeNTFS);
		} catch (error) {
			rawExtraFieldNTFS = new Uint8Array(0);
		}
	} else {
		rawExtraFieldNTFS = rawExtraFieldExtendedTimestamp = new Uint8Array(0);
	}
	const fileEntry = {
		version: version || VERSION_DEFLATE,
		versionMadeBy,
		zip64,
		directory: Boolean(directory),
		filenameUTF8: true,
		rawFilename,
		commentUTF8: true,
		rawComment,
		rawExtraFieldZip64: zip64 ? new Uint8Array(EXTRAFIELD_LENGTH_ZIP64 + 4) : new Uint8Array(0),
		rawExtraFieldExtendedTimestamp,
		rawExtraFieldNTFS,
		rawExtraFieldAES,
		rawExtraField,
		extendedTimestamp,
		msDosCompatible,
		internalFileAttribute,
		externalFileAttribute
	};
	let uncompressedSize = fileEntry.uncompressedSize = 0;
	let bitFlag = BITFLAG_LANG_ENCODING_FLAG;
	if (dataDescriptor) {
		bitFlag = bitFlag | BITFLAG_DATA_DESCRIPTOR;
	}
	let compressionMethod = COMPRESSION_METHOD_STORE;
	if (compressed) {
		compressionMethod = COMPRESSION_METHOD_DEFLATE;
	}
	if (zip64) {
		fileEntry.version = fileEntry.version > VERSION_ZIP64 ? fileEntry.version : VERSION_ZIP64;
	}
	if (encrypted) {
		bitFlag = bitFlag | BITFLAG_ENCRYPTED;
		if (!zipCrypto) {
			fileEntry.version = fileEntry.version > VERSION_AES ? fileEntry.version : VERSION_AES;
			compressionMethod = COMPRESSION_METHOD_AES;
			if (compressed) {
				fileEntry.rawExtraFieldAES[9] = COMPRESSION_METHOD_DEFLATE;
			}
		}
	}
	fileEntry.compressionMethod = compressionMethod;
	const headerArray = fileEntry.headerArray = new Uint8Array(26);
	const headerView = getDataView(headerArray);
	setUint16(headerView, 0, fileEntry.version);
	setUint16(headerView, 2, bitFlag);
	setUint16(headerView, 4, compressionMethod);
	const dateArray = new Uint32Array(1);
	const dateView = getDataView(dateArray);
	let lastModDate;
	if (options.lastModDate < MIN_DATE) {
		lastModDate = MIN_DATE;
	} else if (options.lastModDate > MAX_DATE) {
		lastModDate = MAX_DATE;
	} else {
		lastModDate = options.lastModDate;
	}
	setUint16(dateView, 0, (((lastModDate.getHours() << 6) | lastModDate.getMinutes()) << 5) | lastModDate.getSeconds() / 2);
	setUint16(dateView, 2, ((((lastModDate.getFullYear() - 1980) << 4) | (lastModDate.getMonth() + 1)) << 5) | lastModDate.getDate());
	const rawLastModDate = dateArray[0];
	setUint32(headerView, 6, rawLastModDate);
	setUint16(headerView, 22, rawFilename.length);
	const extraFieldLength = rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length + rawExtraFieldNTFS.length + fileEntry.rawExtraField.length;
	setUint16(headerView, 24, extraFieldLength);
	const localHeaderArray = new Uint8Array(30 + rawFilename.length + extraFieldLength);
	const localHeaderView = getDataView(localHeaderArray);
	setUint32(localHeaderView, 0, LOCAL_FILE_HEADER_SIGNATURE);
	arraySet(localHeaderArray, headerArray, 4);
	arraySet(localHeaderArray, rawFilename, 30);
	arraySet(localHeaderArray, rawExtraFieldAES, 30 + rawFilename.length);
	arraySet(localHeaderArray, rawExtraFieldExtendedTimestamp, 30 + rawFilename.length + rawExtraFieldAES.length);
	arraySet(localHeaderArray, rawExtraFieldNTFS, 30 + rawFilename.length + rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length);
	arraySet(localHeaderArray, fileEntry.rawExtraField, 30 + rawFilename.length + rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length + rawExtraFieldNTFS.length);
	let result;
	let compressedSize = 0;
	if (reader) {
		uncompressedSize = fileEntry.uncompressedSize = reader.size;
		const codec = await createCodec(config.Deflate, {
			codecType: CODEC_DEFLATE,
			level,
			password,
			encryptionStrength,
			zipCrypto: encrypted && zipCrypto,
			passwordVerification: encrypted && zipCrypto && (rawLastModDate >> 8) & 0xFF,
			signed: true,
			compressed,
			encrypted,
			useWebWorkers
		}, config);
		await writer.writeUint8Array(localHeaderArray);
		fileEntry.dataWritten = true;
		result = await processData(codec, reader, writer, 0, uncompressedSize, config, { onprogress, signal });
		compressedSize = result.length;
	} else {
		await writer.writeUint8Array(localHeaderArray);
		fileEntry.dataWritten = true;
	}
	let dataDescriptorArray = new Uint8Array(0);
	let dataDescriptorView, dataDescriptorOffset = 0;
	if (dataDescriptor) {
		dataDescriptorArray = new Uint8Array(zip64 ? (dataDescriptorSignature ? 24 : 20) : (dataDescriptorSignature ? 16 : 12));
		dataDescriptorView = getDataView(dataDescriptorArray);
		if (dataDescriptorSignature) {
			dataDescriptorOffset = 4;
			setUint32(dataDescriptorView, 0, DATA_DESCRIPTOR_RECORD_SIGNATURE);
		}
	}
	if (reader) {
		const signature = result.signature;
		if ((!encrypted || zipCrypto) && signature !== undefined) {
			setUint32(headerView, 10, signature);
			fileEntry.signature = signature;
			if (dataDescriptor) {
				setUint32(dataDescriptorView, dataDescriptorOffset, signature);
			}
		}
		if (zip64) {
			const rawExtraFieldZip64View = getDataView(fileEntry.rawExtraFieldZip64);
			setUint16(rawExtraFieldZip64View, 0, EXTRAFIELD_TYPE_ZIP64);
			setUint16(rawExtraFieldZip64View, 2, EXTRAFIELD_LENGTH_ZIP64);
			setUint32(headerView, 14, MAX_32_BITS);
			setBigUint64(rawExtraFieldZip64View, 12, BigInt(compressedSize));
			setUint32(headerView, 18, MAX_32_BITS);
			setBigUint64(rawExtraFieldZip64View, 4, BigInt(uncompressedSize));
			if (dataDescriptor) {
				setBigUint64(dataDescriptorView, dataDescriptorOffset + 4, BigInt(compressedSize));
				setBigUint64(dataDescriptorView, dataDescriptorOffset + 12, BigInt(uncompressedSize));
			}
		} else {
			setUint32(headerView, 14, compressedSize);
			setUint32(headerView, 18, uncompressedSize);
			if (dataDescriptor) {
				setUint32(dataDescriptorView, dataDescriptorOffset + 4, compressedSize);
				setUint32(dataDescriptorView, dataDescriptorOffset + 8, uncompressedSize);
			}
		}
	}
	if (dataDescriptor) {
		await writer.writeUint8Array(dataDescriptorArray);
	}
	const length = localHeaderArray.length + compressedSize + dataDescriptorArray.length;
	Object.assign(fileEntry, { compressedSize, lastModDate, rawLastModDate, creationDate, lastAccessDate, encrypted, length });
	return fileEntry;
}

async function closeFile(zipWriter, comment, options) {
	const writer = zipWriter.writer;
	const files = zipWriter.files;
	let offset = 0;
	let directoryDataLength = 0;
	let directoryOffset = zipWriter.offset;
	let filesLength = files.size;
	for (const [, fileEntry] of files) {
		directoryDataLength += 46 +
			fileEntry.rawFilename.length +
			fileEntry.rawComment.length +
			fileEntry.rawExtraFieldZip64.length +
			fileEntry.rawExtraFieldAES.length +
			fileEntry.rawExtraFieldExtendedTimestamp.length +
			fileEntry.rawExtraFieldNTFS.length +
			fileEntry.rawExtraField.length;
	}
	let zip64 = options.zip64 || zipWriter.options.zip64 || false;
	if (directoryOffset >= MAX_32_BITS || directoryDataLength >= MAX_32_BITS || filesLength >= MAX_16_BITS) {
		if (options.zip64 === false || zipWriter.options.zip64 === false) {
			throw new Error(ERR_UNSUPPORTED_FORMAT);
		} else {
			zip64 = true;
		}
	}
	const directoryArray = new Uint8Array(directoryDataLength + (zip64 ? ZIP64_END_OF_CENTRAL_DIR_TOTAL_LENGTH : END_OF_CENTRAL_DIR_LENGTH));
	const directoryView = getDataView(directoryArray);
	if (comment && comment.length) {
		if (comment.length <= MAX_16_BITS) {
			setUint16(directoryView, offset + 20, comment.length);
		} else {
			throw new Error(ERR_INVALID_COMMENT);
		}
	}
	for (const [indexFileEntry, fileEntry] of Array.from(files.values()).entries()) {
		const {
			rawFilename,
			rawExtraFieldZip64,
			rawExtraFieldAES,
			rawExtraField,
			rawComment,
			versionMadeBy,
			headerArray,
			directory,
			zip64,
			msDosCompatible,
			internalFileAttribute,
			externalFileAttribute
		} = fileEntry;
		let rawExtraFieldExtendedTimestamp;
		let rawExtraFieldNTFS;
		if (fileEntry.extendedTimestamp) {
			rawExtraFieldNTFS = fileEntry.rawExtraFieldNTFS;
			rawExtraFieldExtendedTimestamp = new Uint8Array(9);
			const extraFieldExtendedTimestampView = getDataView(rawExtraFieldExtendedTimestamp);
			setUint16(extraFieldExtendedTimestampView, 0, EXTRAFIELD_TYPE_EXTENDED_TIMESTAMP);
			setUint16(extraFieldExtendedTimestampView, 2, rawExtraFieldExtendedTimestamp.length - 4);
			setUint8(extraFieldExtendedTimestampView, 4, 0x1);
			setUint32(extraFieldExtendedTimestampView, 5, Math.floor(fileEntry.lastModDate.getTime() / 1000));
		} else {
			rawExtraFieldNTFS = rawExtraFieldExtendedTimestamp = new Uint8Array(0);
		}
		const extraFieldLength = rawExtraFieldZip64.length + rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length + rawExtraFieldNTFS.length + rawExtraField.length;
		setUint32(directoryView, offset, CENTRAL_FILE_HEADER_SIGNATURE);
		setUint16(directoryView, offset + 4, versionMadeBy);
		arraySet(directoryArray, headerArray, offset + 6);
		setUint16(directoryView, offset + 30, extraFieldLength);
		setUint16(directoryView, offset + 32, rawComment.length);
		setUint32(directoryView, offset + 34, internalFileAttribute);
		if (externalFileAttribute) {
			setUint32(directoryView, offset + 38, externalFileAttribute);
		} else if (directory && msDosCompatible) {
			setUint8(directoryView, offset + 38, FILE_ATTR_MSDOS_DIR_MASK);
		}
		if (zip64) {
			setUint32(directoryView, offset + 42, MAX_32_BITS);
		} else {
			setUint32(directoryView, offset + 42, fileEntry.offset);
		}
		arraySet(directoryArray, rawFilename, offset + 46);
		arraySet(directoryArray, rawExtraFieldZip64, offset + 46 + rawFilename.length);
		arraySet(directoryArray, rawExtraFieldAES, offset + 46 + rawFilename.length + rawExtraFieldZip64.length);
		arraySet(directoryArray, rawExtraFieldExtendedTimestamp, offset + 46 + rawFilename.length + rawExtraFieldZip64.length + rawExtraFieldAES.length);
		arraySet(directoryArray, rawExtraFieldNTFS, offset + 46 + rawFilename.length + rawExtraFieldZip64.length + rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length);
		arraySet(directoryArray, rawExtraField, offset + 46 + rawFilename.length + rawExtraFieldZip64.length + rawExtraFieldAES.length + rawExtraFieldExtendedTimestamp.length + rawExtraFieldNTFS.length);
		arraySet(directoryArray, rawComment, offset + 46 + rawFilename.length + extraFieldLength);
		offset += 46 + rawFilename.length + extraFieldLength + rawComment.length;
		if (options.onprogress) {
			try {
				options.onprogress(indexFileEntry + 1, files.size, new Entry(fileEntry));
			} catch (error) {
				// ignored
			}
		}
	}
	if (zip64) {
		setUint32(directoryView, offset, ZIP64_END_OF_CENTRAL_DIR_SIGNATURE);
		setBigUint64(directoryView, offset + 4, BigInt(44));
		setUint16(directoryView, offset + 12, 45);
		setUint16(directoryView, offset + 14, 45);
		setBigUint64(directoryView, offset + 24, BigInt(filesLength));
		setBigUint64(directoryView, offset + 32, BigInt(filesLength));
		setBigUint64(directoryView, offset + 40, BigInt(directoryDataLength));
		setBigUint64(directoryView, offset + 48, BigInt(directoryOffset));
		setUint32(directoryView, offset + 56, ZIP64_END_OF_CENTRAL_DIR_LOCATOR_SIGNATURE);
		setBigUint64(directoryView, offset + 64, BigInt(directoryOffset) + BigInt(directoryDataLength));
		setUint32(directoryView, offset + 72, ZIP64_TOTAL_NUMBER_OF_DISKS);
		filesLength = MAX_16_BITS;
		directoryOffset = MAX_32_BITS;
		directoryDataLength = MAX_32_BITS;
		offset += 76;
	}
	setUint32(directoryView, offset, END_OF_CENTRAL_DIR_SIGNATURE);
	setUint16(directoryView, offset + 8, filesLength);
	setUint16(directoryView, offset + 10, filesLength);
	setUint32(directoryView, offset + 12, directoryDataLength);
	setUint32(directoryView, offset + 16, directoryOffset);
	await writer.writeUint8Array(directoryArray);
	if (comment && comment.length) {
		await writer.writeUint8Array(comment);
	}
}

function sliceAsArrayBuffer(blob, start, end) {
	if (blob.arrayBuffer) {
		if (start || end) {
			return blob.slice(start, end).arrayBuffer();
		} else {
			return blob.arrayBuffer();
		}
	} else {
		const fileReader = new FileReader();
		return new Promise((resolve, reject) => {
			fileReader.onload = event => resolve(event.target.result);
			fileReader.onerror = () => reject(fileReader.error);
			fileReader.readAsArrayBuffer(start || end ? blob.slice(start, end) : blob);
		});
	}
}

async function writeBlob(writer, blob, start = 0) {
	const blockSize = 512 * 1024 * 1024;
	await writeSlice();

	async function writeSlice() {
		if (start < blob.size) {
			const arrayBuffer = await sliceAsArrayBuffer(blob, start, start + blockSize);
			await writer.writeUint8Array(new Uint8Array(arrayBuffer));
			start += blockSize;
			await writeSlice();
		}
	}
}

function getTimeNTFS(date) {
	if (date) {
		return ((BigInt(date.getTime()) + BigInt(11644473600000)) * BigInt(10000));
	}
}

function getOptionValue(zipWriter, options, name) {
	return options[name] === undefined ? zipWriter.options[name] : options[name];
}

function getMaximumCompressedSize(uncompressedSize) {
	return uncompressedSize + (5 * (Math.floor(uncompressedSize / 16383) + 1));
}

function setUint8(view, offset, value) {
	view.setUint8(offset, value);
}

function setUint16(view, offset, value) {
	view.setUint16(offset, value, true);
}

function setUint32(view, offset, value) {
	view.setUint32(offset, value, true);
}

function setBigUint64(view, offset, value) {
	view.setBigUint64(offset, value, true);
}

function arraySet(array, typedArray, offset) {
	array.set(typedArray, offset);
}

function getDataView(array) {
	return new DataView(array.buffer);
}

/*
 Copyright (c) 2022 Gildas Lormeau. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

configure({ Deflate: ZipDeflate, Inflate: ZipInflate });

var zipNoWorker = /*#__PURE__*/Object.freeze({
	__proto__: null,
	configure: configure,
	getMimeType: getMimeType,
	terminateWorkers: terminateWorkers,
	ERR_ABORT: ERR_ABORT,
	Reader: Reader,
	Writer: Writer,
	TextReader: TextReader,
	TextWriter: TextWriter,
	Data64URIReader: Data64URIReader,
	Data64URIWriter: Data64URIWriter,
	BlobReader: BlobReader,
	BlobWriter: BlobWriter,
	Uint8ArrayReader: Uint8ArrayReader,
	Uint8ArrayWriter: Uint8ArrayWriter,
	HttpReader: HttpReader,
	HttpRangeReader: HttpRangeReader,
	WritableStreamWriter: WritableStreamWriter,
	ERR_HTTP_RANGE: ERR_HTTP_RANGE,
	ZipReader: ZipReader,
	ERR_BAD_FORMAT: ERR_BAD_FORMAT,
	ERR_EOCDR_NOT_FOUND: ERR_EOCDR_NOT_FOUND,
	ERR_EOCDR_ZIP64_NOT_FOUND: ERR_EOCDR_ZIP64_NOT_FOUND,
	ERR_EOCDR_LOCATOR_ZIP64_NOT_FOUND: ERR_EOCDR_LOCATOR_ZIP64_NOT_FOUND,
	ERR_CENTRAL_DIRECTORY_NOT_FOUND: ERR_CENTRAL_DIRECTORY_NOT_FOUND,
	ERR_LOCAL_FILE_HEADER_NOT_FOUND: ERR_LOCAL_FILE_HEADER_NOT_FOUND,
	ERR_EXTRAFIELD_ZIP64_NOT_FOUND: ERR_EXTRAFIELD_ZIP64_NOT_FOUND,
	ERR_ENCRYPTED: ERR_ENCRYPTED,
	ERR_UNSUPPORTED_ENCRYPTION: ERR_UNSUPPORTED_ENCRYPTION,
	ERR_UNSUPPORTED_COMPRESSION: ERR_UNSUPPORTED_COMPRESSION,
	ERR_INVALID_SIGNATURE: ERR_INVALID_SIGNATURE,
	ERR_INVALID_PASSWORD: ERR_INVALID_PASSWORD,
	ZipWriter: ZipWriter,
	ERR_DUPLICATED_NAME: ERR_DUPLICATED_NAME,
	ERR_INVALID_COMMENT: ERR_INVALID_COMMENT,
	ERR_INVALID_ENTRY_NAME: ERR_INVALID_ENTRY_NAME,
	ERR_INVALID_ENTRY_COMMENT: ERR_INVALID_ENTRY_COMMENT,
	ERR_INVALID_VERSION: ERR_INVALID_VERSION,
	ERR_INVALID_EXTRAFIELD_TYPE: ERR_INVALID_EXTRAFIELD_TYPE,
	ERR_INVALID_EXTRAFIELD_DATA: ERR_INVALID_EXTRAFIELD_DATA,
	ERR_INVALID_ENCRYPTION_STRENGTH: ERR_INVALID_ENCRYPTION_STRENGTH,
	ERR_UNSUPPORTED_FORMAT: ERR_UNSUPPORTED_FORMAT
});

export { zipNoWorker as default };
