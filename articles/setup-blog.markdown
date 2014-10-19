Title: Setup A Nodejs Based Blog with Wheat
Author: Fangmin Lv
Date: Sun Oct 19 17:37:29 CST 2014
Categories: nodejs, git, blog

In this article I will show you how to setup a NodeJS/Git powered blog by using the <a href="https://github.com/creationix/wheat/" target="_blank">Wheat engine</a> created by [Tim Caswell](https://twitter.com/creationix).

The idea is really simple, just push the blog articles to git content repository, which will trigger the post-receive hook on the git repository. The hook handler will update the git bare repository on blog server. Finally when blog request in, the wheat engine will read the bare repository, and serves up our blog pages.

Here is the pic used to describe the workflow:

![areas](./setup-blog/blog.png)


##Steps to create the blog server

Setup the blog server only need several steps:

###1. Create the Content Repository

The repository should follow the specified file structure: authors, articles, description.markdown and skins.

	$ mkdir blog; cd blog
	$ git init
	$ mkdir articles; mkdir authors; mkdir skins
	$ add the skin resources, description.markdown
	$ git add *; git commit -m "Init commit"
	$ git remote add upstream git@github.com:USER/blog.git
	$ git push -u upstream master

 - articles will contain our post blogs
 - authors maintains the informations about the author
 - skins contain the blog layout and resources, full of haml templates used to render the blog


###2. Create Bare Repository on Server

The second repository we need to create on the blog server is the bare repository. For those of you unfamiliar with git bare repositories, bare repositories only contain the .git folder and no working copies of the files in the repository. Bare repositories are primarily used for sharing, allowing different developers / teams to push their local repositories into the bare repositories. A bare repository cannot perform a git pull, as it doesn't have a working copy of the files. As the bare repository doesn't have a working copy of the files in the repository, the wheat engine will read the content from the git content repository directly, allowing it to apply aggresively caching on it.

We can clone our existing git content repository as as bare repository by command:

	git clone --bare git@github.com:USER/blog.git
    git remote add origin git@github.com:USER/blog.git

###3. Add Web Hook

GitHub offers a feature called WebHook URLs allowing you to add a url to take advantage of git’s post-receive hook. So we use this feature to notify our blog server when there are some modified articles or new articles.

The hook handler is a simple nodejs server which will update the bare repository by fetching the changes:

<setup-blog/post_hook.js>

###4. Implement Blog Server

With Wheat it's really easy about creating the server:

<setup-blog/server.js>

###5. Add Comments Service

The Wheat engine is using `Disqus` as the comment service provider, so we need to acquire a new account on `Disqus`, then add the embedded disqus script to the article.haml under the skin folder.

##Usage

Please help to contribute and share your great articles.  If this is your first article, then please add an entry for yourself in the authors directory as well.

### Article format
Every article is a markdown file with some meta-data at the top of the file.

    Title: Setup blog
    Author: Fangmin Lv
    Date: Thu Aug 18 2013 02:24:35 GMT-0600 (CST)

    Content goes here.

### Author format

Every author has a markdown file located in `authors` folder.

    Github:   your_github_account
    Email:    your_email@example.com

    A few words about you.

##Conclusion

It's really easy to setup such a shared blog system with The <a href="https://github.com/creationix/wheat/" target="_blank">Wheat engine</a> created by [Tim Caswell](https://twitter.com/creationix).

Also it's easy to create and publish articles, all we need to do is just pushing the articles to the git. I really really like the way to manage our articles as our code, so I highly Highly recommended your guys to try this!

##References

- [GitHub Post Receive Hooks](https://help.github.com/articles/post-receive-hooks)
- [Markdown reference](http://daringfireball.net/projects/markdown/basics)
- [Disqus service](http://disqus.com/)
- [Wheat by creationix](https://github.com/creationix/wheat)
