# Allango

This is a personal blog used to memo and share knowledges.

The blog is powered by a new static blog engine written in node called [wheat][].

[wheat]: http://github.com/creationix/wheat

## Contributing

The wheat blog engine is a shared blog framework, peoples could cooperate to share through the same blog.
If this is your first article, then please add an entry for yourself in the authors directory as well.

### Article format

Every article is a markdown file with some meta-data at the top of the file.

    Title: Control Flow in Node
    Author: Allan Lv
    Date: Thu Feb 04 2013 02:24:35 GMT-0600 (CST)

    I had so much fun writing the last article on control flow, that I decided to...

    ## First section: Display JavaScript files

    * display contents of external JavaScript file (path is relative to .markdown file)
    <test-code/test-file.js>

    * display contents of external JavaScript file and evaluate its contents
    <test-code/evaluate-file.js*>

    More content goes here.

### Author format

Every author has a markdown file located in `authors` folder. You should name this file by your name and surname `Name Surname.markdown`.

    Github:   your_github_account
    Email:    your_email@example.com
    Homepage: http://yourhomepage.com
    Location: City, State, Country

    A few words about you.

## Licensing

All articles are copyright to the individual authors.  Authors can put notes about license and copyright on their individual bio pages if they wish.
