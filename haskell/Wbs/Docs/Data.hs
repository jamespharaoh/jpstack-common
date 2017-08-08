{-# LANGUAGE OverloadedStrings #-}

module Wbs.Docs.Data where

import qualified Data.String.Utils as StringUtils

import           Wbs.TagLang (Tag (..))
import qualified Wbs.TagLang as TagLang

data ElemDoc =
		ElemDoc {
			elemId :: String,
			elemType :: String,
			elemName :: String,
			elemDescription :: [String]
		}
	deriving Show

tagWithName :: String -> [Tag] -> Maybe Tag
tagWithName name tags =
	case filter ((== name) . tagName) tags of
		[] -> Nothing
		[tag] -> Just tag
		_ -> error $ "Multiple tags: " ++ name

tagNameVal :: String -> [Tag] -> Maybe String
tagNameVal name tags =
	case tagWithName name tags of
		Just tag -> Just $ tagValue tag
		Nothing -> Nothing

tagNameValReq :: String -> [Tag] -> String
tagNameValReq name tags =
	case tagNameVal name tags of
		Just value -> value
		Nothing -> error $ "No tag: " ++ name

tagNameValDef :: String -> String -> [Tag] -> String
tagNameValDef name def tags =
	case tagNameVal name tags of
		Just value -> value
		Nothing -> def

tagToElem :: Tag -> Either String ElemDoc
tagToElem tag = do

	_ <- if tagName tag /= "elem"
		then return $ Left "Invalid tag name"
		else return $ Right ()

	let children = tagChildren tag

	let [ elemType, elemName ] =
		StringUtils.split "/" $ tagValue tag

	return ElemDoc {
		elemId = tagValue tag,
		elemType = elemType,
		elemName = elemName,
		elemDescription = tagText tag
	}
