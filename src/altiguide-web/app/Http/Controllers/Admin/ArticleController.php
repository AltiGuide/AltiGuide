<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Article;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

class ArticleController extends Controller
{
    /**
     * Simpan artikel baru.
     */
    public function store(Request $request)
    {
        $request->validate([
            'title'       => 'required|string|max:255',
            'content'     => 'required|string',
            'category'    => 'required|in:tips,info,safety,news',
            'status'      => 'required|in:draft,published',
            'excerpt'     => 'nullable|string|max:500',
            'mountain_id' => 'nullable|exists:mountains,id',
            'cover_image' => 'nullable|image|max:5120',
        ]);

        $coverPath = null;
        if ($request->hasFile('cover_image')) {
            $coverPath = $request->file('cover_image')->store('articles', 'public');
        }

        Article::create([
            'admin_id'     => Auth::guard('admin')->id(),
            'mountain_id'  => $request->mountain_id ?: null,
            'title'        => $request->title,
            'slug'         => Str::slug($request->title) . '-' . strtolower(Str::random(4)),
            'excerpt'      => $request->excerpt,
            'content'      => $request->content,
            'cover_image'  => $coverPath,
            'category'     => $request->category,
            'status'       => $request->status,
            'published_at' => $request->status === 'published' ? now() : null,
        ]);

        return back()->with('success', 'Artikel berhasil dipublikasikan.');
    }

    /**
     * Update artikel.
     */
    public function update(Request $request, $id)
    {
        $article = Article::findOrFail($id);

        $request->validate([
            'title'       => 'required|string|max:255',
            'content'     => 'required|string',
            'category'    => 'required|in:tips,info,safety,news',
            'status'      => 'required|in:draft,published',
            'excerpt'     => 'nullable|string|max:500',
            'mountain_id' => 'nullable|exists:mountains,id',
            'cover_image' => 'nullable|image|max:5120',
        ]);

        $data = $request->only(['title', 'content', 'excerpt', 'category', 'status']);
        $data['mountain_id'] = $request->mountain_id ?: null;

        if ($request->hasFile('cover_image')) {
            // Hapus gambar lama jika ada
            if ($article->cover_image) {
                Storage::disk('public')->delete($article->cover_image);
            }
            $data['cover_image'] = $request->file('cover_image')->store('articles', 'public');
        }

        // Set published_at saat pertama kali dipublish
        if ($request->status === 'published' && !$article->published_at) {
            $data['published_at'] = now();
        }

        $article->update($data);

        return back()->with('success', 'Artikel berhasil diperbarui.');
    }

    /**
     * Hapus artikel.
     */
    public function destroy($id)
    {
        $article = Article::findOrFail($id);

        if ($article->cover_image) {
            Storage::disk('public')->delete($article->cover_image);
        }

        $article->delete();

        return back()->with('success', 'Artikel berhasil dihapus.');
    }
}
