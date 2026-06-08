const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

const hrefsToFix = ['/', '/article', '/login', '/dashboard'];

walkDir('resources/js/Pages', function(filePath) {
    if (filePath.endsWith('.vue')) {
        let content = fs.readFileSync(filePath, 'utf8');
        let newContent = content;
        
        for (let href of hrefsToFix) {
            let escapedHref = href.replace(/\//g, '\\/');
            let regex = new RegExp('<Link([^>]*href="' + escapedHref + '"[^>]*)>([\\s\\S]*?)<\\/Link>', 'g');
            newContent = newContent.replace(regex, '<a$1>$2</a>');
        }
        
        if (content !== newContent) {
            fs.writeFileSync(filePath, newContent);
            console.log('Updated', filePath);
        }
    }
});
