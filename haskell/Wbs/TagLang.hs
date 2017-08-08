module Wbs.TagLang where

import Text.ParserCombinators.Parsec
import Control.Applicative hiding ((<|>), optional, many)

-- tag data structure used to represent all data

data Tag =
		Tag {
			tagName :: String,
			tagValue :: String,
			tagText :: [String],
			tagChildren :: [Tag]
		}
	deriving Show

-- node data structure, used while parsing (why?)

data Node =
		  TagNode Tag
		| TextNode String
	deriving Show

isTagNode :: Node -> Bool
isTagNode (TagNode _) = True
isTagNode _ = False

isTextNode :: Node -> Bool
isTextNode (TextNode _) = True
isTextNode _ = False

fromTextNode :: Node -> String
fromTextNode (TextNode value) = value

fromTagNode :: Node -> Tag
fromTagNode (TagNode value) = value

-- parser

parseWhitespace :: Parser String
parseWhitespace = many $ oneOf [' ']

parseBlank :: Parser String
parseBlank = do
	white <- many $ oneOf [' ', '\t']

	oneOf ['\n']

	--if white == ""
	--	then oneOf ['\n'] *> return ()
	--	else optional $ oneOf ['\n']

	return white

parseIndent :: Int -> Parser String
parseIndent 0 = return ""
parseIndent n = do oneOf "\t"; parseIndent (n - 1)

parseTagName :: Parser String
parseTagName = oneOf "@" *> many (noneOf [' ', '\n'])

parseTagValue :: Parser String
parseTagValue = parseWhitespace *> many (noneOf ['\n'])

parseTag :: Int -> Parser Tag
parseTag indent = do

	many $ try $ parseBlank

	parseIndent indent
	tagName <- parseTagName
	tagValue <- parseTagValue
	oneOf ['\n']

	childNodes <- many $ try $ parseNode (indent + 1)

	return Tag {
		tagName = tagName,
		tagValue = tagValue,
		tagText = map fromTextNode . filter isTextNode $ childNodes,
		tagChildren = map fromTagNode . filter isTagNode $ childNodes
	}

parseTagNode :: Int -> Parser Node
parseTagNode indent = do
	tag <- parseTag indent
	return $ TagNode tag

parseTextLine :: Int -> Parser String
parseTextLine indent = do
	parseIndent indent
	line <- many (noneOf ['\n'])
	oneOf ['\n']
	return line

parseTextNode :: Int -> Parser Node
parseTextNode indent = do
	many $ try $ parseBlank
	lines <- many1 $ parseTextLine indent
	return $ TextNode $ concat lines

parseNode :: Int -> Parser Node
parseNode indent =
	try (parseTagNode indent) <|> parseTextNode indent

parseDocument :: Parser [Tag]
parseDocument = do
	nodes <- many $ try $ parseTag 0
	many $ parseBlank
	eof
	return nodes

parseFile :: String -> IO (Either ParseError [Tag])
parseFile filename = do
	input <- readFile filename
	return $ parse parseDocument filename input
