<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;

class Article extends Model
{
    use HasUuids;

    protected $fillable = [
        'admin_id',
        'mountain_id',
        'title',
        'slug',
        'excerpt',
        'content',
        'cover_image',
        'category',
        'status',
        'published_at',
    ];

    protected $casts = [
        'published_at' => 'datetime',
    ];

    // ──── Relationships ────

    public function admin()
    {
        return $this->belongsTo(Admin::class);
    }

    public function mountain()
    {
        return $this->belongsTo(Mountain::class);
    }
}
