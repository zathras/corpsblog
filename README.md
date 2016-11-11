# CorpsBlog static site generator
-------

# This is not working software!
![Danger Image](docs/images/danger.jpg "Danger!")

As of this writing, this is not functional software.  I'm just working
on it.

# Corpsblog
CorpsBlog is yet another static site builder.  I took a look around at
things like [Jekyll](http://jekyllrb.com/) and [JBake](http://jbake.org/), 
and after playing around I got tired of
figuring out multi-layered complex software, just to make a simple blog.
I figured that if I kept all of the logic in pure Kotlin/Java, I'd end up
with a system that's easier to understand and extend.  I have some
specific needs in mind:

*  I want a responsive site (one that works well with smartphones
   and tablets).
*  I want to have photo galleries that are entirely self-contained on my site.
*  I want to be able to embed video and audio clips, and have automated
   uploads to e.g. YouTube.
*  I want to be able to design and preview entirely off-line.
*  I want a completely automated upload process, designed around the idea that
   I might only have access to the internet every couple of weeks from
   an internet cafe.
*  I want to host on Github pages, where I have the rest of my web sites.
*  If I think of something else, I want to be able to easily extend
   my site generator, without referring to any external documentation
   or depending on anything that I might not have on my local computer.

Kotlin is a pefectly expressive language, so I see no need to invent yet
more languages on top of it, like a template language.

# Exernal Code

I grabbed useful stuff from the following places:

*  [Txtmark](https://github.com/rjeschke/txtmark) for a nice, simple
   markdown processor written in Java.
*  [Jbake-uno](https://github.com/tisseurdetoile/jbake-uno) for a
   web theme for the blog.  I took the assets, and translated the
   relevant templates into Kotlin code by hand.
*  [PhotoSwipe](http://photoswipe.com/) for a JavaScript image gallery.
*  The [JHeader](https://sourceforge.net/projects/jheader/?source=directory )
   library to read JPEG EXIF data, so I can auto-rotate images in a gallery.

# The Name "CorpsBlog"

My intent is to provide simple, "core" blogging functionality for my Peace Corps
blog.   OK, I also want to be in complete control...  I tried using [JBake](http://jbake.org/), but in order
to add automatic gallery generation, I'd have had to have waded through too many layers
of integration, JRuby for asciiDoctor, and a sizeable templating framework.  It seemed easier to 
just write my own site generator, and it's certainly more joyful.

Perhaps I'll come up with a better pun for a new name someday...  Maybe something
involving [Mooré](https://en.wikipedia.org/wiki/Mossi_language) :-)

#Output Directory Structure

* index.html
* posts
  * yyyy-mm-dd-optional-name.html
  * yyyy-mm-dd-optional-name-gallery
    * 1.jpg
    * 2.jpg
  * yyyy-mm-dd-optional-name-gallery-2
    * 1.jpg
    * 2.jpg
    * 3.jpg

