package com.jovial.lib.html

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 *
 * Initial version copied from com.example.html at
 * http://kotlinlang.org/docs/reference/type-safe-builders.html
 */

private val yymmddDateFormat = SimpleDateFormat("yyyy-MM-dd")

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

/**
 * An element that consists only of text, like the body of a paragraph.
 */
class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = arrayListOf<Pair<String, String>>()
        // key/value pairs, kept in order to avoid weird-looking HTML

    /**
     * Add an attribute if the value isn't null
     */
    fun addAttribute(k: String, v: String?) {
        if (v != null) {
            attributes += Pair(k, v)
        }
    }
    protected open fun isSelfClosing() : Boolean = false    // like <meta foo="bar"/>
    protected open fun noEndTag() : Boolean = false         // like <img src="foo.png">

    protected fun <T : Element> initTag(tag: T, init: (T.() -> Unit)?): T {
        if (init != null) {
            tag.init()
        }
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        if (isSelfClosing() && children.size == 0) {
            builder.append("$indent<$name${renderAttributes()}/>\n")
        } else {
            builder.append("$indent<$name${renderAttributes()}>\n")
            if (noEndTag()) {
                if (children.size > 0) {
                    throw RuntimeException("Internal error:  Children in a $name tag")
                }
            } else {
                for (c in children) {
                    c.render(builder, indent + "  ")
                }
                builder.append("$indent</$name>\n")
            }
        }
    }

    protected fun renderAttributes(): String? {
        val builder = StringBuilder()
        for (a in attributes) {
            builder.append(" ${a.first}=\"${a.second}\"")
        }
        return builder.toString()
    }


    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class HTML() : TagWithText("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)

    fun body(init: Body.() -> Unit) = initTag(Body(), init)

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("<!DOCTYPE html>\n")
        super.render(builder, indent)
    }
}

abstract class HeadTag(name: String) : TagWithText(name) {
    fun include(expr: HeadTag.() -> Unit) {
        expr()
    }

    fun meta(charset: String? = null,
             name: String? = null,
             author: String? = null,
             content: String? = null) {
        val t = initTag(Meta(), {})
        t.addAttribute("charset", charset)
        t.addAttribute("name", name)
        t.addAttribute("author", author)
        t.addAttribute("content", content)
    }
    fun link(href: String,
             rel: String,
             title: String? = null,
             type: String? = null)
    {
        val t = initTag(Link(), {})
        t.attributes += Pair("href", href)
        t.attributes += Pair("rel", rel)
        t.addAttribute("title", title)
        t.addAttribute("type", type)
    }
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun script(src: String? = null,
               type: String? = null,
               init: (ScriptInHead.() -> Unit)? = null)
    {
        val t = initTag(ScriptInHead(), init)
        t.addAttribute("src", src)
        t.addAttribute("type", type)
    }
}

class Head() : HeadTag("head")
class Title() : HeadTag("title")
class Meta() : HeadTag("meta") {
    override protected fun isSelfClosing() : Boolean = true
}
class Link() : HeadTag("link") {
    override protected fun isSelfClosing() : Boolean = true
}

class ScriptInHead() : HeadTag("script")

abstract class BodyTag(name: String) : TagWithText(name) {
    fun include(expr: BodyTag.() -> Unit) {
        expr()
    }
    fun script(src: String? = null,
               type: String? = null,
               init: (Script.() -> Unit)? = null)
    {
        val t = initTag(Script(), init)
        t.addAttribute("src", src)
        t.addAttribute("type", type)
    }
    fun b(init: B.() -> Unit) = initTag(B(), init)
    fun em(init: Em.() -> Unit) = initTag(Em(), init)
    fun p(style: String? = null,
          name: String? = null,
          content: String? = null,
          class_: String? = null,
          init: P.() -> Unit)
    {
        val t = initTag(P(), init)
        t.addAttribute("style", style)
        t.addAttribute("name", name)
        t.addAttribute("content", content)
        t.addAttribute("class", class_)
    }
    fun div(class_: String? = null, id: String? = null, init: Div.() -> Unit) {
        val t = initTag(Div(), init)
        t.addAttribute("id", id)
        t.addAttribute("class", class_)
    }
    fun nav(class_: String, init: Nav.() -> Unit) {
        val t = initTag(Nav(), init)
        t.attributes += Pair("class", class_)
    }
    fun article(class_: String, init: Article.() -> Unit) {
        val t = initTag(Article(), init)
        t.attributes += Pair("class", class_)
    }
    fun section(class_: String, init: Section.() -> Unit) {
        val t = initTag(Section(), init)
        t.attributes += Pair("class", class_)
    }
    fun header(class_: String, style: String? = null, init: Header.() -> Unit) {
        val t = initTag(Header(), init)
        t.attributes += Pair("class", class_)
        t.addAttribute("style", style)
    }
    fun span(class_: String, init: Span.() -> Unit) {
        val t = initTag(Span(), init)
        t.attributes += Pair("class", class_)
    }
    fun i(class_: String? = null, init: I.() -> Unit) {
        val t = initTag(I(), init)
        t.addAttribute("class", class_)
    }
    fun time(datetime: Date, class_: String? = null, init: Time.() -> Unit) {
        val t = initTag(Time(), init)
        t.addAttribute("datetime", yymmddDateFormat.format(datetime))
        t.addAttribute("class", class_)
    }
    fun h1(class_: String? = null, init: H1.() -> Unit) {
        val t = initTag(H1(), init)
        t.addAttribute("class", class_)
    }
    fun h2(class_: String? = null, init: H2.() -> Unit) {
        val t = initTag(H2(), init)
        t.addAttribute("class", class_)
    }
    fun ul(class_: String? = null, init: Ul.() -> Unit) {
        val t = initTag(Ul(), init)
        t.addAttribute("class", class_)
    }
    fun li(class_: String? = null, init: Li.() -> Unit) {
        val t = initTag(Li(), init)
        t.addAttribute("class", class_)
    }
    fun a(href: String, title: String? = null, target: String? = null,
          class_: String? = null, init: A.() -> Unit) {
        val t = initTag(A(), init)
        t.addAttribute("href", href)
        t.addAttribute("title", title)
        t.addAttribute("target", target)
        t.addAttribute("class", class_)
    }
    fun img(src: String, class_: String? = null, alt: String? = null) {
        val t = initTag(Img(), {})
        t.addAttribute("src", src)
        t.addAttribute("class", class_)
        if (alt == null) {
            t.addAttribute("alt", "*")
        } else {
            t.addAttribute("alt", alt)
        }
    }
    fun hr(class_: String?) {
        val t = initTag(Hr(), {})
        t.addAttribute("class", class_)
    }
    fun noscript(init: Noscript.() -> Unit) = initTag(Noscript(), init)
    fun footer(class_: String, init: Footer.() -> Unit) {
        val t = initTag(Footer(), init)
        t.attributes += Pair("class", class_)
    }
}

class Body() : BodyTag("body")
class Script() : BodyTag("script")
class B() : BodyTag("b")
class Em : BodyTag("em")
class P() : BodyTag("p")
class H1() : BodyTag("h1")
class H2() : BodyTag("h2")
class Ul() : BodyTag("ul")
class Li() : BodyTag("li")

class Div() : BodyTag("div")
class Nav() : BodyTag("nav")

class Header() : BodyTag("header")
class Span() : BodyTag("span")
class I() : BodyTag("i")
class Time() : BodyTag("time")

class Article() : BodyTag("article")

class Section() : BodyTag("section")


class A() : BodyTag("a")
class Img() : BodyTag("img") {
    override protected fun noEndTag() : Boolean = true
}
class Hr() : BodyTag("hr") {
    override protected fun noEndTag() : Boolean = true
}
class Noscript() : BodyTag("noscript")
class Footer() : BodyTag("footer")



fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}

fun head(init: Head.() -> Unit) : Head {
    val head = Head()
    head.init()
    return head
}

class BodyFragment : BodyTag("") {
    override fun render(builder: StringBuilder, indent: String) {
        for (c in children) {
            c.render(builder, indent)
        }
    }
}

fun bodyFragment(init: BodyFragment.() -> Unit) : BodyFragment {
    val fragment = BodyFragment()
    fragment.init()
    return fragment
}
