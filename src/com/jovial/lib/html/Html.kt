package com.jovial.lib.html

/**
 * Created by w.foote on 11/3/2016.
 *
 * Initial version copied from com.example.html at
 * http://kotlinlang.org/docs/reference/type-safe-builders.html
 */


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

    protected open fun isSelfClosing() : Boolean = false

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
            for (c in children) {
                c.render(builder, indent + "  ")
            }
            builder.append("$indent</$name>\n")
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

open class HtmlLiteral() : Element {
    val children = arrayListOf<TextElement>()

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    override fun render(builder: StringBuilder, indent: String) {
        for (c in children) {
            c.render(builder, indent)   // No added indent
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}

class HtmlComment() : HtmlLiteral() {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<!--\n")
        super.render(builder, indent)
        builder.append("-->\n")
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

abstract class HeadOrBody(name: String) : TagWithText(name) {
    fun literal(init: HtmlLiteral.() -> Unit) {
        initTag(HtmlLiteral(), init)
    }
    fun script(src: String? = null,
               type: String? = null,
               init: (Script.() -> Unit)? = null)
    {
        val t = initTag(Script(), init)
        if (src != null) t.attributes += Pair("src", src)
        if (type != null) t.attributes += Pair("type", type)
    }
    fun comment(init: HtmlComment.() -> Unit) {
        initTag(HtmlComment(), init)
    }
}

class Head() : HeadOrBody("head") {
    fun meta(charset: String? = null,
             name: String? = null,
             author: String? = null,
             content: String? = null) {
        val t = initTag(Meta(), {})
        if (charset != null) t.attributes += Pair("charset", charset)
        if (name != null) t.attributes += Pair("name", name)
        if (author != null) t.attributes += Pair("author", author)
        if (content != null) t.attributes += Pair("content", content)
    }
    fun link(href: String,
             rel: String,
             title: String? = null,
             type: String? = null)
    {
        val t = initTag(Link(), {})
        t.attributes += Pair("href", href)
        t.attributes += Pair("rel", rel)
        if (title != null) t.attributes += Pair("title", title)
        if (type != null) t.attributes += Pair("type", type)
    }
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun include(expr: Head.() -> Unit) {
        expr()
    }
}

class Title() : TagWithText("title")
class Meta() : TagWithText("meta") {
    override protected fun isSelfClosing() : Boolean = true
}
class Link() : TagWithText("link") {
    override protected fun isSelfClosing() : Boolean = true
}

class Script() : TagWithText("script")

abstract class BodyTag(name: String) : HeadOrBody(name) {
    fun b(init: B.() -> Unit) = initTag(B(), init)
    fun em(init: Em.() -> Unit) = initTag(Em(), init)
    fun p(style: String? = null,
          name: String? = null,
          content: String? = null,
          init: P.() -> Unit)
    {
        val p = initTag(P(), init)
        if (style != null) p.attributes += Pair("style", style)
        if (name != null) p.attributes += Pair("name", name)
        if (content != null) p.attributes += Pair("content", content)
    }
    fun div(class_: String, init: Div.() -> Unit) {
        val t = initTag(Div(), init)
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
    fun header(class_: String, init: Header.() -> Unit) {
        val t = initTag(Header(), init)
        t.attributes += Pair("class", class_)
    }
    fun h1(init: H1.() -> Unit) = initTag(H1(), init)
    fun a(href: String, init: A.() -> Unit) {
        val a = initTag(A(), init)
        a.attributes += Pair("href", href)
    }

    fun include(expr: BodyTag.() -> Unit) {
        expr()
    }
}

class Body() : BodyTag("body")
class B() : BodyTag("b")
class Em : BodyTag("em")
class P() : BodyTag("p")
class H1() : BodyTag("h1")

class Div() : BodyTag("div")

class Header() : BodyTag("header")

class Article() : BodyTag("article")

class Section() : BodyTag("section")


class A() : BodyTag("a")



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
