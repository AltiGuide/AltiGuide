const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

walkDir('resources/js/Pages', function(filePath) {
    if (filePath.endsWith('.vue')) {
        let content = fs.readFileSync(filePath, 'utf8');
        
        let newContent = content.replace(/<Link[^>]*>Weather Analytics<\/Link>/g, '<a href="/weather" class="hover:text-[#374426] transition-colors">Weather Analytics</a>');
        newContent = newContent.replace(/<a[^>]*>Weather Analytics<\/a>/g, '<a href="/weather" class="hover:text-[#374426] transition-colors">Weather Analytics</a>');
        
        if (content !== newContent) {
            fs.writeFileSync(filePath, newContent);
            console.log('Updated', filePath);
        }
    }
});
